package com.fiap.hackgov.DTOs.Employee;

import com.fiap.hackgov.entities.enums.Roles;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

public record CreateEmployeeDTO(


        @NotBlank(message = "Name is required")
        String name,


        @NotBlank(message = "CPF is required")
        @CPF(message = "Invalid CPF")
        String cpf,


        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,

        @NotBlank(message = "Password is required")
        String password,


        boolean status,


        @NotNull(message = "Role is required")
        @Enumerated(EnumType.STRING)
        Roles role,


        String avatarPath,


        @NotBlank(message = "Phone is required")
        @Pattern(message = "Invalid phone number", regexp = "^\\d{10}$")
        String phone,


        boolean twoFactor,

        @NotNull(message = "City Hall ID is required")
        UUID cityHallId
) {}