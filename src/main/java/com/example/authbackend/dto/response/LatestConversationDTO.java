package com.example.authbackend.dto.response;

import java.time.LocalDateTime;

public record LatestConversationDTO(
    Long conversationId,
    String lastSenderName,
    String lastSenderAvatarUrl,
    String lastMessageContent,
    LocalDateTime lastMessageTimestamp
    //Todo avturl
){}
