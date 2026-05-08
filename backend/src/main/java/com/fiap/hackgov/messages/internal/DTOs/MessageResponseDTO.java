package com.fiap.hackgov.messages.internal.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponseDTO(

        UUID id,

        UUID senderId,
        String senderName,

        UUID conversationId,

        String content,

        LocalDateTime sentAt,

        Boolean readMessage
) {
}