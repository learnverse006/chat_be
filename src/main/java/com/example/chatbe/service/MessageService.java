package com.example.chatbe.service;

import com.example.chatbe.dto.ws.MessagePayLoad;
import com.example.chatbe.entity.Conversation;
import com.example.chatbe.entity.ConversationMember;
import com.example.chatbe.entity.Message;
import com.example.chatbe.entity.User;
import com.example.chatbe.enums.ConversationType;
import com.example.chatbe.enums.MessageType;
import com.example.chatbe.repository.ConversationMemberRepository;
import com.example.chatbe.repository.ConversationRepository;
import com.example.chatbe.repository.MessageRepository;
import com.example.chatbe.repository.UserRepository;
import com.example.chatbe.util.AesUtil;
import com.example.chatbe.ws.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.time.LocalDateTime;
import java.util.List;

@Service
//@AllArgsConstructor
@RequiredArgsConstructor
public class MessageService {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final MessageRepository messageRepository;
    private final SessionManager sessionManager;

    public void handleSingleMessage(MessagePayLoad payload){
        Long senderId = payload.getSenderId();
        Long receiverId = payload.getReceiverId();
        String content = payload.getContent();
        MessageType messageType = payload.getMessageType();

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Conversation conversation = conversationRepository
                .findConversationBetweenUsers(senderId, receiverId, ConversationType.SINGLE)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setType(ConversationType.SINGLE);
                    conversationRepository.save(newConv);

                    ConversationMember m1 = new ConversationMember();
                    m1.setUser(sender);
                    m1.setConversation(newConv);

                    ConversationMember m2 = new ConversationMember();
                    m2.setUser(receiver);
                    m2.setConversation(newConv);

                    conversationMemberRepository.saveAll(List.of(m1, m2));
                    return newConv;
                });

        Message message = new Message();
        message.setSender(sender);
        message.setConversation(conversation);
        message.setContent(AesUtil.encrypt(content));
        message.setType(messageType);
        message.setTimestamp(LocalDateTime.now());

        messageRepository.save(message);

        if (sessionManager.isOnline(receiverId)) {
            try {
                sessionManager.getSession(receiverId)
                        .sendMessage(new TextMessage(content));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handleGroupMessage(MessagePayLoad payload) {
        Long senderId = payload.getSenderId();
        Long conversationId = payload.getConversationId();
        String content = payload.getContent();
        MessageType messageType = payload.getMessageType();

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (conversation.getType() != ConversationType.GROUP) {
            throw new RuntimeException("Conversation is not of type GROUP");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setConversation(conversation);
        message.setContent(AesUtil.encrypt(content));
        message.setType(messageType);
        message.setTimestamp(LocalDateTime.now());

        messageRepository.save(message);

        List<ConversationMember> members = conversationMemberRepository.findByConversation(conversation);

        for (ConversationMember member : members) {
            Long memberId = member.getUser().getId();
            if (!memberId.equals(senderId) && sessionManager.isOnline(memberId)) {
                try {
                    sessionManager.getSession(memberId)
                            .sendMessage(new TextMessage(content));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
