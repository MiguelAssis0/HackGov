package com.fiap.hackgov.messages.internal.DTOs.chat;

import com.fiap.hackgov.messages.internal.entities.enums.ChatType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatDTO(

        UUID id,

        String title,

        ChatType type,

        UUID cityHallId,

        List<ChatParticipantDTO> participants,

        LocalDateTime createdAt

) {
}
