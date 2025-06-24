package com.example.chatbe.controller;


import com.example.chatbe.dto.group.GroupMessageHistoryResponse;
import com.example.chatbe.dto.response.LatestConversationResponse;
import com.example.chatbe.dto.single.CreateSingleConversationRequest;
import com.example.chatbe.dto.single.SingleMessageHistoryResponse;
import com.example.chatbe.entity.Conversation;
import com.example.chatbe.entity.ConversationMember;
import com.example.chatbe.entity.Message;
import com.example.chatbe.entity.User;
import com.example.chatbe.enums.ConversationType;
import com.example.chatbe.repository.ConversationMemberRepository;
import com.example.chatbe.repository.ConversationRepository;
import com.example.chatbe.repository.MessageRepository;
import com.example.chatbe.repository.UserRepository;
import com.example.chatbe.service.UserService;
import com.example.chatbe.util.AesUtil;
import com.example.chatbe.ws.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final ConversationMemberRepository conversationMemberRepository;

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;





    @GetMapping("/me")
    public Map<String, String> getCurrentUser(Authentication authentication){
        String email = authentication.getName();
        return Map.of("email", email);
    }

    @GetMapping("/conversations/latest")
    public ResponseEntity<List<LatestConversationResponse>> getLatestConversations(@RequestParam Long userId) {
        List<Long> conversationIds = conversationMemberRepository.findAllConversationIdsByUserId(userId);
        List<Message> lastMessages = messageRepository.findLatestMessages(conversationIds);

        List<LatestConversationResponse> result = lastMessages.stream().map(msg -> {
            Conversation conv = msg.getConversation();
            ConversationType type = conv.getType();
            String decryptedContent;

            try {
                decryptedContent = AesUtil.decrypt(msg.getContent());
            } catch (Exception e) {
                decryptedContent = "[Không đọc được nội dung]";
            }

            String name;
            String avatar;

            if (type == ConversationType.SINGLE) {
                List<ConversationMember> members = conversationMemberRepository.findByConversation(conv);
                User other = members.stream()
                        .map(ConversationMember::getUser)
                        .filter(u -> !u.getId().equals(userId))
                        .findFirst()
                        .orElse(null);

                name = other != null ? other.getFullName() : "[Đã rời]";
                avatar = other != null ? other.getAvatarUrl() : "";
            } else {
                name = conv.getName();
                avatar = "[Group]";
            }

            boolean isUnread = conversationMemberRepository
                    .findByConversationIdAndUserId(conv.getId(), userId)
                    .map(cm -> msg.getTimestamp().isAfter(cm.getLastReadAt() != null ? cm.getLastReadAt() : LocalDateTime.MIN))
                    .orElse(true);

            User sender = msg.getSender();

            return new LatestConversationResponse(
                    conv.getId(),
                    type,
                    name,
                    avatar,
                    decryptedContent,
                    msg.getTimestamp(),
                    isUnread,
                    sender.getId(),
                    sender.getFullName(),
                    sender.getAvatarUrl()
            );
        }).toList();

        return ResponseEntity.ok(result);
    }


    @GetMapping("/online-status")
    public ResponseEntity<List<Long>> checkOnlineStatus(@RequestParam List<Long> ids) {
        List<Long> onlineIds = ids.stream()
                .filter(sessionManager::isOnline)
                .toList();

        return ResponseEntity.ok(onlineIds);
    }

    @PostMapping("/single")
    public ResponseEntity<String> createSingleConversation(@RequestBody CreateSingleConversationRequest request) {
        if (Objects.equals(request.getUser1Id(), request.getUser2Id())) {
            return ResponseEntity.badRequest().body("Cannot create conversation with yourself");
        }

        Optional<Conversation> existing = conversationRepository
                .findConversationBetweenUsers(request.getUser1Id(), request.getUser2Id(), ConversationType.SINGLE);

        if (existing.isPresent()) {
            return ResponseEntity.ok("Conversation already exists with ID: " + existing.get().getId());
        }

        User user1 = userRepository.findById(request.getUser1Id())
                .orElseThrow(() -> new RuntimeException("User 1 not found"));
        User user2 = userRepository.findById(request.getUser2Id())
                .orElseThrow(() -> new RuntimeException("User 2 not found"));

        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.SINGLE);
        conversationRepository.save(conversation);

        ConversationMember cm1 = new ConversationMember();
        cm1.setConversation(conversation);
        cm1.setUser(user1);
        cm1.setLastReadAt(LocalDateTime.now());

        ConversationMember cm2 = new ConversationMember();
        cm2.setConversation(conversation);
        cm2.setUser(user2);
        cm2.setLastReadAt(LocalDateTime.now());

        conversationMemberRepository.saveAll(List.of(cm1, cm2));

        return ResponseEntity.ok("Conversation created with ID: " + conversation.getId());
    }

    @GetMapping("/single/history")
    public ResponseEntity<List<SingleMessageHistoryResponse>> loadSingleChatHistory(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit)
    {
        Optional<Conversation> conversationOpt = conversationRepository
                .findConversationBetweenUsers(senderId, receiverId, ConversationType.SINGLE);
        if (conversationOpt.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Conversation conversation = conversationOpt.get();
        List<Message> messages = messageRepository.findByConversationOrderByTimestampDesc(
                conversation,
                PageRequest.of(page, limit)
        );

        List<SingleMessageHistoryResponse> result = messages.stream()
                .map(m -> {
                    String decrypted;
                    try {
                        decrypted = AesUtil.decrypt(m.getContent());
                    } catch (Exception e) {
                        decrypted = "[Lỗi giải mã]";
                    }

                    return new SingleMessageHistoryResponse(
                            m.getSender().getId(),
                            decrypted,
                            m.getTimestamp()
                    );
                })
                .toList();

        return ResponseEntity.ok(result);
    }


    @GetMapping("/group/history")
    public ResponseEntity<List<GroupMessageHistoryResponse>> loadGroupChatHistory(
            @RequestParam Long userId,
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty() || conversationOpt.get().getType() != ConversationType.GROUP) {
            return ResponseEntity.badRequest().build();
        }

        Conversation conversation = conversationOpt.get();

        Optional<ConversationMember> memberOpt =
                conversationMemberRepository.findByConversationIdAndUserId(conversationId, userId);
        memberOpt.ifPresent(cm -> {
            cm.setLastReadAt(LocalDateTime.now());
            conversationMemberRepository.save(cm);
        });

        List<Message> messages = messageRepository.findByConversationOrderByTimestampDesc(
                conversation,
                PageRequest.of(page, limit)
        );

        List<GroupMessageHistoryResponse> results = messages.stream()
                .map(m -> {
                    String decrypted;
                    try {
                        decrypted = AesUtil.decrypt(m.getContent());
                    } catch (Exception e) {
                        decrypted = "[Lỗi giải mã]";
                    }

                    return new GroupMessageHistoryResponse(
                            m.getSender().getId(),
                            m.getSender().getFullName(),
                            decrypted,
                            m.getTimestamp()
                    );
                })
                .toList();

        return ResponseEntity.ok(results);
    }

}
