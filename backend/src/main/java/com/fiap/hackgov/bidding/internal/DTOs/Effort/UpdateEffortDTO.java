package com.fiap.hackgov.bidding.internal.DTOs.Effort;

import com.fiap.hackgov.bidding.internal.entities.enums.KindCommitment;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateEffortDTO(

        KindCommitment kindCommitment,

        @DecimalMin(value = "0.01", message = "Reserved value must be greater than zero")
        @Digits(integer = 13, fraction = 2, message = "Reserved value must have at most 13 integer digits and 2 decimal digits")
        BigDecimal reservedValue,

        UUID emitterId,
        UUID contractId,
        UUID executionOrderId,
        UUID paymentStatementId

) {}