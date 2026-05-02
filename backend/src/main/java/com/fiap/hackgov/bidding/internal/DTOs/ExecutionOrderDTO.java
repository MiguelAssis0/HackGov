package com.fiap.hackgov.bidding.internal.DTOs;

import com.fiap.hackgov.bidding.internal.entities.enums.OrderType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record ExecutionOrderDTO(
        UUID id,
        String number,
        OrderType type,
        Date emissionDate,
        Date expectedDeliveryDate,
        Date actualDeliveryDate,
        String status,
        UUID responsibleId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
