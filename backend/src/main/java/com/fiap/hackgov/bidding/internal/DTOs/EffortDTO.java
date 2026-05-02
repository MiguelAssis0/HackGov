package com.fiap.hackgov.bidding.internal.DTOs;

import com.fiap.hackgov.bidding.internal.entities.enums.KindCommitment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EffortDTO(
        UUID id,
        KindCommitment kindCommitment,
        BigDecimal reservedValue,
        UUID emitterId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ContractDTO contract,
        ExecutionOrderDTO executionOrder,
        PaymentStatementDTO paymentStatement
) {
}
