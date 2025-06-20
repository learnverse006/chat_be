package com.example.authbackend.dto.request;

import com.example.authbackend.enums.MessageType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MessageRequest {
    private Long senderId;
    private Long receiverId;
    private String content;
    private MessageType type;
}
