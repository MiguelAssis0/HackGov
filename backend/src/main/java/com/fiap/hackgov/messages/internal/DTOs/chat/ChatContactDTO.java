package com.fiap.hackgov.messages.internal.DTOs.chat;

import java.util.UUID;

public record ChatContactDTO(

        UUID id,

        String fullName,

        String avatarPath,

        String occupationName,

        String sectorName

) {
}
