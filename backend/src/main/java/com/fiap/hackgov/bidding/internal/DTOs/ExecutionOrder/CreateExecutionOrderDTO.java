package com.fiap.hackgov.bidding.internal.DTOs.ExecutionOrder;

import com.fiap.hackgov.bidding.internal.entities.enums.OrderType;
import jakarta.validation.constraints.*;

import java.util.Date;
import java.util.UUID;

public record CreateExecutionOrderDTO(

        @NotNull(message = "Order type is required")
        OrderType orderType,

        @NotBlank(message = "Number is required")
        @Size(max = 50)
        String number,

        @Size(max = 1000)
        String description,

        @NotNull(message = "Emission date is required")
        @PastOrPresent
        Date emissionDate,

        @Future(message = "Expected delivery date must be in the future")
        Date expectedDeliveryDate,

        @NotNull(message = "Responsible ID is required")
        UUID responsibleId,

        @NotNull(message = "Contract ID is required")
        UUID contractId,

        UUID effortId

) {}