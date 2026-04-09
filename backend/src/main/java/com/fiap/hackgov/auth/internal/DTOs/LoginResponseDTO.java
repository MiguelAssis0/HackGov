package com.fiap.hackgov.auth.internal.DTOs;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String email,
        String name,
        Roles role,
        boolean requiresTwoFactor
) {}
