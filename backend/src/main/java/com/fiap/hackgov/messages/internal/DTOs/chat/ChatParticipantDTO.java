package com.fiap.hackgov.messages.internal.DTOs.chat;

import com.fiap.hackgov.messages.internal.entities.enums.ChatRole;

import java.util.UUID;

public record ChatParticipantDTO(

        UUID employeeId,

        String fullName,

        String avatarPath,

        ChatRole role

) {
}
