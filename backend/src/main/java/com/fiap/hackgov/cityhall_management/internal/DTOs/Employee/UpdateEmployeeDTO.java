package com.fiap.hackgov.cityhall_management.internal.DTOs.Employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateEmployeeDTO(
        String firstName,
        String lastName,
        @Email String email,
        @CPF String cpf,
        String phone,
        String cep,
        String registrationNumber,
        UUID sectorId,
        UUID occupationId,
        Double salary,
        Double hoursWorked,
        LocalDateTime admissionDate,
        LocalDateTime dismissalDate,
        Boolean status,
        Boolean isAdminCidade
) {
}
