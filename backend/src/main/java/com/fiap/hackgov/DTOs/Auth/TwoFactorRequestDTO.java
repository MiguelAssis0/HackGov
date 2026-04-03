package com.fiap.hackgov.DTOs.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TwoFactorRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,
        
        @NotBlank(message = "Two-factor code is required")
        @Pattern(message = "Code must be 6 digits", regexp = "^\\d{6}$")
        String code
) {}
