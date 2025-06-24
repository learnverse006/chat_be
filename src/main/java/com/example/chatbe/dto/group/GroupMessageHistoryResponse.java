package com.example.chatbe.dto.group;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GroupMessageHistoryResponse {
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
}
