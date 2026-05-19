package com.fiap.hackgov.bidding.internal.DTOs.processStatus;

import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProcessStateDTO(

        @NotNull(message = "Stage is required")
        ProcessStage currentStage,

        @NotNull(message = "Step number is required")
        Integer numberStep,

        @NotNull(message = "Bidding process is required")
        UUID biddingProcessId,

        UUID responsibleId,

        String observation

) {
}