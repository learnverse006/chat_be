package com.example.chatbe.dto.response;

import com.example.chatbe.enums.MessageType;
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
