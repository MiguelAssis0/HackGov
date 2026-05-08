package com.fiap.hackgov.messages.internal.DTOs;

import java.util.List;
import java.util.UUID;

public record CreateConversationRequestDTO(
        List<UUID> participantIds

) {
}