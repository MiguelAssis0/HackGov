package com.fiap.hackgov.bidding.internal.DTOs.executionOrder;

import com.fiap.hackgov.bidding.internal.entities.enums.ExecutionOrderType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExecutionOrderResponseDTO(
        UUID id,
        UUID contractId,
        String contractNumber,
        ExecutionOrderType type,
        String number,
        String description,
        LocalDate issuedAt,
        UUID issuedById,
        String issuedByName,
        LocalDateTime createdAt
) {
}
