package com.example.authbackend.service;

import com.example.authbackend.entity.Conversation;
import com.example.authbackend.entity.ConversationMember;
import com.example.authbackend.entity.Message;
import com.example.authbackend.entity.User;
import com.example.authbackend.enums.ConversationType;
import com.example.authbackend.enums.MessageType;
import com.example.authbackend.repository.ConversationMemberRepository;
import com.example.authbackend.repository.ConversationRepository;
import com.example.authbackend.repository.MessageRepository;
import com.example.authbackend.repository.UserRepository;
import com.example.authbackend.util.AesUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
//@AllArgsConstructor
public class MessageService {
    private final UserRepository userRepository;

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final MessageRepository messageRepository;

    public MessageService(UserRepository userRepository, ConversationRepository conversationRepository,
                          ConversationMemberRepository conversationMemberRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Sends a message from sender to receiver.
     *
     * @param senderId   ID of the sender
     * @param receiverId ID of the receiver
     * @param type       Type of the message (e.g., TEXT, IMAGE)
     * @param content    Content of the message
     * @return The saved Message entity
     */
    public Message sendMessage(Long senderId, Long receiverId, MessageType type, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        //get conversation between sender and receiver
        Conversation conversation = conversationRepository
                .findConversationBetweenUsers(senderId, receiverId, ConversationType.SINGLE).orElseGet(()->{
                    Conversation newConversation = new Conversation();
                    newConversation.setType(ConversationType.SINGLE);
                    conversationRepository.save(newConversation);

                    ConversationMember m1 = new ConversationMember();
                    m1.setConversation(newConversation);
                    m1.setUser(sender);

                    ConversationMember m2 = new ConversationMember();
                    m2.setConversation(newConversation);
                    m2.setUser(receiver);

                    conversationMemberRepository.saveAll(List.of(m1, m2));
                    return newConversation;
        });

        // send Message
        String encryptedContent = AesUtil.encrypt(content);
        Message message = new Message();
        message.setSender(sender);
        message.setConversation(conversation);
        message.setContent(encryptedContent);
        message.setType(type);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);

    }

    public String decrypt(Message message) {
        return AesUtil.decrypt(message.getContent());
    }


    public List<Message> loadConversationHistory(Long senderId, Long receiverId, int limit) {
        Conversation conversation = conversationRepository
                .findConversationBetweenUsers(senderId, receiverId, ConversationType.SINGLE)
                .orElse(null);

        if (conversation == null) {
            return List.of(); // Trả về danh sách rỗng nếu không có hội thoại
        }

        return messageRepository.findByConversationOrderByTimestampDesc(
                conversation,
                PageRequest.of(0, limit)
        );
    }
}
