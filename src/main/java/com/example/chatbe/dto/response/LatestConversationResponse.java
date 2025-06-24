package com.example.chatbe.dto.response;

import com.example.chatbe.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LatestConversationResponse {
    private Long conversationId;
    private ConversationType type;
    private String name;
    private String avatar;
    private String content;
    private LocalDateTime timestamp;
    private boolean unread;

    private Long senderId;
    private String senderName;
    private String senderAvatar;
}