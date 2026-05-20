package com.fiap.hackgov.bidding.internal.DTOs.processStatus;

import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessStateResponseDTO(

        UUID id,
        ProcessStage currentStage,
        Integer numberStep,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        UUID responsibleId,
        String observation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID biddingProcessId

) {
}