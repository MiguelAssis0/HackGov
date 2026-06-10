package com.fiap.hackgov.bidding.internal.DTOs.proporsal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProposalDTO(

        @NotNull(message = "Licitation process id is required") UUID licitationProcessId,

        @NotNull(message = "Supplier id is required") UUID supplierId,

        @NotNull(message = "Proposed value is required") @DecimalMin(value = "0.01", message = "Proposed value must be greater than zero") BigDecimal proposedValue,

        @Size(max = 1000, message = "Observation must contain at most 1000 characters") String observation

) {
}