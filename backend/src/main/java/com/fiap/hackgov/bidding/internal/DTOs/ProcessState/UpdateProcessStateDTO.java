package com.fiap.hackgov.bidding.internal.DTOs.ProcessState;

import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateProcessStateDTO(

        ProcessStage currentStage,
        Integer numberStep,
        LocalDateTime finishedAt,
        UUID responsibleId,
        String observation

) {}