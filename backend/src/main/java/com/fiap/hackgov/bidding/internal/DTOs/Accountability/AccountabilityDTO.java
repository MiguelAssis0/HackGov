package com.fiap.hackgov.bidding.internal.DTOs.Accountability;

import com.fiap.hackgov.bidding.internal.DTOs.Effort.EffortDTO;
import com.fiap.hackgov.bidding.internal.DTOs.PaymentStatement.PaymentStatementDTO;
import com.fiap.hackgov.bidding.internal.entities.enums.InstallmentStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountabilityDTO(
        UUID id,
        ProcessStage processStage,
        InstallmentStatus installmentStatus,
        UUID responsibleId,
        LocalDateTime analysisDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        EffortDTO effort,
        PaymentStatementDTO paymentStatement
) {
}
