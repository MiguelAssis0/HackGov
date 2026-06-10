package com.fiap.hackgov.bidding.internal.DTOs.requisiton;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignProcurementResponsibleDTO(

        @NotNull(message = "Procurement responsible ID is required")
        UUID employeeId

) {
}
