package com.fiap.hackgov.DTOs.Employee;

import com.fiap.hackgov.entities.enums.Roles;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

public record CreateEmployeeDTO(


        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 100 characters")
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


        @NotNull(message = "Role is required")
        @Enumerated(EnumType.STRING)
        Roles role,


        String avatarPath,


        @NotBlank(message = "Phone is required")
        @Size(min = 8, max = 16, message = "Phone must be 12 characters long")
        String phone,


        boolean twoFactor,

        @NotNull(message = "City Hall ID is required")
        UUID cityHallId
) {}