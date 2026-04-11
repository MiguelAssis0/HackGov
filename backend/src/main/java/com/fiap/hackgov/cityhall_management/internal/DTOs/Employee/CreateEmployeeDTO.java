package com.fiap.hackgov.cityhall_management.internal.DTOs.Employee;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateEmployeeDTO(

        @NotNull(message = "Salary is required")
        @Min(value = 0, message = "Salary must be greater than or equal to 0")
        Double salary,

        @NotNull(message = "Admission date is required")
        LocalDateTime admissionDate,

        @NotBlank(message = "Registration number is required")
        @Pattern(
                regexp = "^\\d{7}$",
                message = "Registration number must contain exactly 7 digits"
        )
        String registrationNumber,

        @NotNull(message = "Hours worked is required")
        @Min(value = 0, message = "Hours worked must be greater than or equal to 0")
        Double hoursWorked,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "CPF is required")
        @CPF(message = "CPF must be valid")
        String cpf,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "Phone is required")
        String phone,

        @NotNull(message = "Role is required")
        int role,

        @NotNull(message = "City hall ID is required")
        UUID cityhallId
) {}