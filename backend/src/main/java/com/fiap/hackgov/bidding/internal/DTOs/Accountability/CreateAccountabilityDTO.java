package com.fiap.hackgov.bidding.internal.DTOs.Accountability;

import com.fiap.hackgov.bidding.internal.entities.enums.InstallmentStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAccountabilityDTO(

        @NotNull(message = "Process stage is required")
        ProcessStage processStage,

        @NotNull(message = "Installment status is required")
        InstallmentStatus installmentStatus,

        @NotNull(message = "Responsible ID is required")
        UUID responsibleId,

        @NotNull(message = "Analysis date is required")
        @PastOrPresent(message = "Analysis date cannot be in the future")
        LocalDateTime analysisDate,

        UUID effortId,

        UUID paymentStatementId

) {
}
