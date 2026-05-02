package com.fiap.hackgov.bidding.internal.DTOs;

import com.fiap.hackgov.bidding.internal.entities.enums.InstallmentStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;

import java.util.Date;
import java.util.UUID;

public record CreateAccountabilityDTO(
        ProcessStage processStage,
        InstallmentStatus installmentStatus,
        UUID responsibleId,
        Date analysisDate,
        UUID effortId,
        UUID paymentStatementId
) {
}
