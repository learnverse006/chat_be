package com.example.chatbe.dto.single;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SingleMessageHistoryResponse {
    private Long senderId;
    private String content;
    private LocalDateTime timestamp;
}