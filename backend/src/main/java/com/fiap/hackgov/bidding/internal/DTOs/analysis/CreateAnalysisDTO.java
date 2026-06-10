package com.fiap.hackgov.bidding.internal.DTOs.analysis;

import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAnalysisDTO(

        @NotNull(message = "Requisition ID is required")
        UUID requisitionId,

        @NotNull(message = "Analysis stage is required")
        ProcessStage stage,

        @Size(max = 1000, message = "Observation must have at most 1000 characters")
        String observation

) {
}
