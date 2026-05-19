package com.fiap.hackgov.bidding.internal.DTOs.contract;

import com.fiap.hackgov.bidding.internal.entities.enums.ContractStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateContractDTO(

        @NotNull(message = "Licitation process id is required")
        UUID licitationProcessId,

        @NotNull(message = "Supplier id is required")
        UUID supplierId,

        @NotBlank(message = "Contract number is required")
        @Size(max = 100, message = "Contract number must have at most 100 characters")
        String contractNumber,

        @NotBlank(message = "Object description is required")
        @Size(min = 10, max = 2000, message = "Object description must contain between 10 and 2000 characters")
        String objectDescription,

        @NotNull(message = "Total value is required")
        @DecimalMin(value = "0.01", message = "Total value must be greater than zero")
        BigDecimal totalValue,

        @NotNull(message = "Signed date is required")
        LocalDate signedAt,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotNull(message = "Responsible id is required")
        UUID responsibleId,

        @NotNull(message = "Contract status is required")
        ContractStatus status
) {
}
