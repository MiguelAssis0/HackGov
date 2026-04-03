package com.fiap.hackgov.DTOs.Employee;

import com.fiap.hackgov.entities.enums.Roles;
import com.fiap.hackgov.entities.enums.RequisitionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeDTO(
        UUID id,
        String name,
        String cpf,
        String email,
        boolean status,
        Roles role,
        String avatarPath,
        String phone,
        boolean twoFactor,
        UUID cityHallId,
        Double salary,
        LocalDateTime admissionDate,
        RequisitionStatus requisitionStatus,
        LocalDateTime dismissalDate,
        String registrationNumber,
        Double hoursWorked,
        LocalDateTime createdAt
) {}
