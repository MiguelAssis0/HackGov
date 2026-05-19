package com.fiap.hackgov.bidding.internal.DTOs.licitation;

import com.fiap.hackgov.bidding.internal.entities.enums.LicitationStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LicitationProcessResponseDTO(

        UUID id,

        String processNumber,

        UUID requisitionId,

        String requisitionNumber,

        LicitationType type,

        LicitationStatus status,

        BigDecimal estimatedValue,

        String objectDescription,

        LocalDate openingDate,

        LocalDate closingDate,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
