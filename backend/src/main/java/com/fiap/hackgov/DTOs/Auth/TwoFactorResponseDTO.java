package com.fiap.hackgov.DTOs.Auth;

public record TwoFactorResponseDTO(
        String accessToken,
        String refreshToken,
        String message
) {}
