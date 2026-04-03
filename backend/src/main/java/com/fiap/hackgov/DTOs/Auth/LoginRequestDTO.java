package com.fiap.hackgov.DTOs.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,
        
        @NotBlank(message = "Password is required")
        String password
) {}
