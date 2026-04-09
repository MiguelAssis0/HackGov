package com.fiap.hackgov.auth.internal.DTOs;

public record TwoFactorResponseDTO(
        String accessToken,
        String refreshToken,
        String message
) {}
