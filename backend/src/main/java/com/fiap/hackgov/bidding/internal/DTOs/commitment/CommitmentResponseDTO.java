package com.fiap.hackgov.bidding.internal.DTOs.commitment;

import com.fiap.hackgov.bidding.internal.entities.enums.CommitmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommitmentResponseDTO(
        UUID id,
        UUID contractId,
        String contractNumber,
        UUID executionOrderId,
        String executionOrderNumber,
        CommitmentType type,
        String commitmentNumber,
        BigDecimal reservedValue,
        UUID issuedById,
        String issuedByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
