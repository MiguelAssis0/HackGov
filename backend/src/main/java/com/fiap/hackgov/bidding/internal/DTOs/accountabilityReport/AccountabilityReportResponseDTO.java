package com.fiap.hackgov.bidding.internal.DTOs.accountabilityReport;

import com.fiap.hackgov.bidding.internal.entities.enums.AccountabilityStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountabilityReportResponseDTO(
        UUID id,
        UUID contractId,
        String contractNumber,
        AccountabilityStatus status,
        String observation,
        UUID responsibleId,
        String responsibleName,
        LocalDate analyzedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
