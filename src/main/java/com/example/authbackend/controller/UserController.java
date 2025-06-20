package com.example.authbackend.controller;


import com.example.authbackend.dto.response.LatestConversationDTO;
import com.example.authbackend.dto.response.UserResponse;
import com.example.authbackend.entity.Message;
import com.example.authbackend.entity.User;
import com.example.authbackend.repository.ConversationMemberRepository;
import com.example.authbackend.repository.MessageRepository;
import com.example.authbackend.service.UserService;
import com.example.authbackend.util.AesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final ConversationMemberRepository conversationMemberRepository;

    @Autowired
    private MessageRepository messageRepository;

    public UserController(UserService userService, ConversationMemberRepository conversationMemberRepository) {
        this.userService = userService;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    @GetMapping("/me")
    public Map<String, String> getCurrentUser(Authentication authentication){
        String email = authentication.getName();
        return Map.of("email", email);
    }

    @GetMapping("/conversations/latest")
    public ResponseEntity<List<LatestConversationDTO>> getLatestConversations(@RequestParam Long userId) {

        List<Long> conversationIds = conversationMemberRepository.findAllConversationIdsByUserId(userId);

        List<Message> lastMessages =  messageRepository.findLatestMessages(conversationIds);

        List<LatestConversationDTO> result = lastMessages.stream().map(msg -> {
            User sender = msg.getSender();
            String decryptedContent;
            try {
                decryptedContent = AesUtil.decrypt(msg.getContent());
            } catch (Exception e) {
                decryptedContent = "[Không đọc được nội dung]";
            }

            return new LatestConversationDTO(
                    msg.getConversation().getId(),
                    sender.getFullName(),
                    sender.getAvatarUrl(),
                    decryptedContent,
                    msg.getTimestamp()
            );
        }).toList();
        return ResponseEntity.ok(result);

    }
}
