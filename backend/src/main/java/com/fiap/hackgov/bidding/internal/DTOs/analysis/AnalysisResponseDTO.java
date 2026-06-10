package com.fiap.hackgov.bidding.internal.DTOs.analysis;

import com.fiap.hackgov.bidding.internal.entities.enums.AnalysisResult;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnalysisResponseDTO(

        UUID id,
        UUID requisitionId,
        ProcessStage stage,
        AnalysisResult result,
        UUID analyzedById,
        String observation,
        LocalDateTime analyzedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
