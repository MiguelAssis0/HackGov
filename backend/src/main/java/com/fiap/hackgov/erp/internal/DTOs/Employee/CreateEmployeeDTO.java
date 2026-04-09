package com.fiap.hackgov.erp.internal.DTOs.Employee;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.erp.internal.entities.CityHall;
import com.fiap.hackgov.erp.internal.entities.EmployeeJobLevel;
import com.fiap.hackgov.erp.internal.entities.enums.RequisitionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CreateEmployeeDTO(

        @NotBlank(message = "Salary is required")
        @Min(value = 0, message = "Salary must be greater than 0")
        Double salary,

        @NotBlank(message = "Admission Date is required")
        LocalDateTime admissionDate,
        @NotBlank
        @Pattern(regexp = "^\\d{7}$", message = "Registration Number must have 7 digits")
        String registrationNumber,

        @NotNull(message = "Hours Worked is required")
        @Min(value = 0, message = "Hours Worked must be greater than 0")
        Double hoursWorked,

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank
        int role,

        @NotNull(message = "City Hall ID is required")
        UUID cityhallId
) {}