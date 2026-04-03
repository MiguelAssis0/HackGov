package com.fiap.hackgov.DTOs.Auth;

public record TwoFactorResponseDTO(
        String token,
        String message
) {}
