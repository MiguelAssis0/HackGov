package com.fiap.hackgov.DTOs.Auth;

import com.fiap.hackgov.entities.enums.Roles;

public record LoginResponseDTO(
        String token,
        String email,
        String name,
        Roles role,
        boolean requiresTwoFactor
) {}
