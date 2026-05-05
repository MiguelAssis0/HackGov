package com.fiap.hackgov.bidding.internal.DTOs.Effort;

import com.fiap.hackgov.bidding.internal.entities.enums.KindCommitment;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateEffortDTO(

        @NotNull(message = "Kind commitment is required")
        KindCommitment kindCommitment,

        @NotNull(message = "Reserved value is required")
        @DecimalMin(value = "0.01", message = "Reserved value must be greater than zero")
        @Digits(integer = 13, fraction = 2, message = "Reserved value must have at most 13 integer digits and 2 decimal digits")
        BigDecimal reservedValue,

        @NotNull(message = "Emitter ID is required")
        UUID emitterId,

        @NotNull(message = "Contract ID is required")
        UUID contractId,

        UUID executionOrderId,

        UUID paymentStatementId

) {
}
