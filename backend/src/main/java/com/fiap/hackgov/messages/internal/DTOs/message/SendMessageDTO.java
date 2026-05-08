package com.fiap.hackgov.messages.internal.DTOs.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendMessageDTO(

        @NotNull(message = "Chat ID is required")
        UUID chatId,

        @NotBlank(message = "Content is required")
        String content

) {
}