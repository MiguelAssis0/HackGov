package com.fiap.hackgov.bidding.internal.DTOs;

import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record RequisitionDTO(
        UUID id,
        String number,
        String description,
        BigDecimal amount,
        Date requestDate,
        Date approvalDate,
        RequestStatus status,
        UUID requesterId,
        UUID approverId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
