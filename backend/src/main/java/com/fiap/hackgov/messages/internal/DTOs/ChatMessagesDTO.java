package com.fiap.hackgov.messages.internal.DTOs;

import java.util.UUID;

public record ChatMessagesDTO(
        UUID receiverId,
        UUID conversationId,
        String content
) {}