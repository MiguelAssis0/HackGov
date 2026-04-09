package com.fiap.hackgov.auth.internal.DTOs.users;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String firstName,
        String lastName,
        String cpf,
        String email,
        boolean status,
        Roles role,
        String avatarPath,
        String phone,
        boolean twoFactor,
        boolean accessibility,
        boolean acceptTerms,
        LocalDateTime lastLogin,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
