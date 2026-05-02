package com.fiap.hackgov.bidding.internal.DTOs;

import com.fiap.hackgov.bidding.internal.entities.enums.InstallmentStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record AccountabilityDTO(
        UUID id,
        ProcessStage processStage,
        InstallmentStatus installmentStatus,
        UUID responsibleId,
        Date analysisDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        EffortDTO effort,
        PaymentStatementDTO paymentStatement
) {
}
