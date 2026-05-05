package com.fiap.hackgov.bidding.internal.DTOs.ExecutionOrder;

import com.fiap.hackgov.bidding.internal.entities.enums.OrderType;
import com.fiap.hackgov.bidding.internal.entities.enums.OrderStatus;
import jakarta.validation.constraints.Future;

import java.util.Date;
import java.util.UUID;

public record UpdateExecutionOrderDTO(

        OrderType orderType,
        String number,
        String description,

        Date emissionDate,

        @Future
        Date expectedDeliveryDate,

        Date actualDeliveryDate,

        OrderStatus status,

        UUID responsibleId,
        UUID contractId,
        UUID effortId

) {}