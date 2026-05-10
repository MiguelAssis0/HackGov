package com.fiap.hackgov.bidding.internal.DTOs.processHistory;

import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessHistoryDTO(

        ProcessStage stage,

        UUID changedById,

        String changedByName,

        String observation,

        LocalDateTime changedAt

) {
}
