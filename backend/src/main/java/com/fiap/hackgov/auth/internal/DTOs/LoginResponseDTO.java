package com.fiap.hackgov.auth.internal.DTOs;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        boolean requiresTwoFactor
) {
}
