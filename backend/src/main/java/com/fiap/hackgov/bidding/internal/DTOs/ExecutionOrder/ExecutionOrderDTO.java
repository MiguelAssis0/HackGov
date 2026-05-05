package com.fiap.hackgov.bidding.internal.DTOs.ExecutionOrder;

import com.fiap.hackgov.bidding.internal.entities.enums.OrderStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.OrderType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record ExecutionOrderDTO(

        UUID id,
        String number,
        OrderType orderType,
        String description,
        Date emissionDate,
        Date expectedDeliveryDate,
        Date actualDeliveryDate,
        OrderStatus status,
        UUID responsibleId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}