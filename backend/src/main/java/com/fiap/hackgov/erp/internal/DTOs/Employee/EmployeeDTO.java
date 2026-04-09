package com.fiap.hackgov.erp.internal.DTOs.Employee;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.erp.internal.entities.enums.RequisitionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeDTO(
        UUID id,
        Double salary,
        LocalDateTime admissionDate,
        RequisitionStatus requisitionStatus,
        LocalDateTime dismissalDate,
        String registrationNumber,
        Double hoursWorked,
        LocalDateTime createdAt,
        UUID userId,
        UUID cityhallId
) {}
