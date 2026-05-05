package com.fiap.hackgov.cityhall_management.internal.DTOs.Employee;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.RequisitionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeDTO(

        // User
        UUID id,
        String firstName,
        String lastName,
        String cpf,
        String email,
        boolean status,
        Roles role,
        String avatarPath,
        String phone,
        boolean twoFactor,
        boolean accessibility,
        LocalDateTime lastLogin,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // Employee
        Double salary,
        LocalDateTime admissionDate,
        RequisitionStatus requisitionStatus,
        LocalDateTime dismissalDate,
        String registrationNumber,
        Double hoursWorked,
        UUID cityHallId

) {}
