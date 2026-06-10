package com.fiap.hackgov.messages.internal.DTOs.chat;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePrivateChatDTO(

        @NotNull(message = "Employee ID is required")
        UUID employeeId
) {
}