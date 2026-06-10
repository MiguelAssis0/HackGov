package com.fiap.hackgov.bidding.internal.DTOs.licitation;

import com.fiap.hackgov.bidding.internal.entities.enums.LicitationEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LicitationHistoryDTO(

        UUID id,

        LicitationEventType eventType,

        LicitationStatus status,

        UUID changedById,

        String changedByName,

        String observation,

        LocalDateTime changedAt

) {
}