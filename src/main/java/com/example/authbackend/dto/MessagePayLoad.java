package com.example.authbackend.dto;

import com.example.authbackend.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessagePayLoad {
    private Long senderId;
    private Long receiverId;
    private MessageType type;
    private String content;
    private String action; /// history, send
}
//{
//        "action": "send",
//        "senderId": 1,
//        "type": "TEXT",
//        "receiverId": 2,
//        "content": "Xin chào client 2!"
//        }