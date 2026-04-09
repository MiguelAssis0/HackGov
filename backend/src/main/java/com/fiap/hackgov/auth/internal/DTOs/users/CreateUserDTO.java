package com.fiap.hackgov.auth.internal.DTOs.users;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateUserDTO(

        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 100 characters")
        @Pattern(message = "Name must contain only letters", regexp = "^[a-zA-Z\s]*$")
        String name,


        @NotBlank(message = "CPF is required")
        @CPF(message = "Invalid CPF")
        String cpf,


        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        @Size(min = 3, max = 100, message = "Email must be between 3 and 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 50, message = "Password must be at least 8 characters long")
        @Pattern(message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character", regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
        String password,

        boolean status,

        boolean accessibility,


        @NotNull(message = "Role is required")
        @Enumerated(EnumType.STRING)
        Roles role,

        String avatarPath,

        @Pattern(
                regexp = "^\\+55\\d{10,11}$",
                message = "Phone must be in format +5511999999999"
        )
        String phone,

        boolean twoFactor
) {
}