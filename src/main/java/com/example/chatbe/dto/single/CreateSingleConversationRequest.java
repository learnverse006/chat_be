package com.example.chatbe.dto.single;

import lombok.Data;

@Data
public class CreateSingleConversationRequest {
    private Long user1Id;
    private Long user2Id;
}