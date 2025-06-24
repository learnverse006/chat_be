package com.example.chatbe.dto.ws;

import com.example.chatbe.enums.ConversationType;
import com.example.chatbe.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessagePayLoad {
    private Long conversationId;
    private Long senderId;
    private Long receiverId; // for single conversation
    private ConversationType conversationType;
    private MessageType messageType;
    private String content;
    private Timestamp timestamp;
//    private String action; /// history, send
}
//{
//        "action": "send",
//        "senderId": 1,
//        "type": "TEXT",
//        "receiverId": 2,
//        "content": "Xin chào client 2!"
//        }