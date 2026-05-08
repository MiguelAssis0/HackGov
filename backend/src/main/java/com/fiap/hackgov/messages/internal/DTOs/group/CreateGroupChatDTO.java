package com.fiap.hackgov.messages.internal.DTOs.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateGroupChatDTO(

        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Participant IDs are required")
        @Size(min = 1, message = "At least one participant ID is required")
        List<UUID> participantIds

) {
}
