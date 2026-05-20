package com.fiap.hackgov.bidding.internal.DTOs.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponseDTO(
        UUID id,
        UUID declarationId,
        UUID commitmentId,
        UUID contractId,
        String contractNumber,
        BigDecimal value,
        Boolean treasuryApproved,
        UUID treasuryResponsibleId,
        String treasuryResponsibleName,
        UUID treasurySectorId,
        String treasurySectorName,
        UUID approvedById,
        String approvedByName,
        LocalDate paidAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
