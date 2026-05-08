package com.fiap.hackgov.messages.internal.DTOs;

import java.util.UUID;

public record SendMessageRequestDTO(

        UUID conversationId,
        String content

) {
}