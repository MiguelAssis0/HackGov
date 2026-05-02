package com.fiap.hackgov.bidding.internal.DTOs;

import com.fiap.hackgov.bidding.internal.entities.enums.KindCommitment;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateEffortDTO(
        KindCommitment kindCommitment,
        BigDecimal reservedValue,
        UUID emitterId,
        UUID contractId,
        UUID executionOrderId,
        UUID paymentStatementId
) {
}
