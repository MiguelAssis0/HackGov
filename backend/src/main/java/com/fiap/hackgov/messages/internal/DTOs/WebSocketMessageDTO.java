package com.fiap.hackgov.messages.internal.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

public record WebSocketMessageDTO(

        UUID conversationId,

        String content,

        UUID senderId,

        String senderName,

        LocalDateTime sentAt
) {
}