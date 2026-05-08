package com.fiap.hackgov.messages.internal.DTOs;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConversationResponseDTO(

        UUID conversationId,

        List<String> participants,

        String lastMessage,

        LocalDateTime lastMessageAt

) {
}