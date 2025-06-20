package com.example.authbackend.dto.response;

import com.example.authbackend.enums.MessageType;
import lombok.Data;

@Data
public class MessageResponse {
    private int id;
    private Long senderId;
    private Long conversationId;
    private String content;
    private MessageType type;
    private String timestamp;
}
