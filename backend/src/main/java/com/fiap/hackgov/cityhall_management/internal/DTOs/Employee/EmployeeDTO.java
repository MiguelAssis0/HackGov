package com.fiap.hackgov.cityhall_management.internal.DTOs.Employee;

import com.fiap.hackgov.cityhall_management.internal.entities.enums.RequisitionStatus;

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
