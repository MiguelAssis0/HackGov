package com.fiap.hackgov.bidding.internal.DTOs.licitation;

import com.fiap.hackgov.bidding.internal.entities.enums.LicitationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateLicitationProcessDTO(

        @NotNull(message = "Requisition id is required")
        UUID requisitionId,

        @NotNull(message = "Responsible employee id is required")
        UUID responsibleId,

        @NotNull(message = "Licitation type is required")
        LicitationType type,

        @NotNull(message = "Estimated value is required")
        @DecimalMin(
                value = "0.01",
                message = "Estimated value must be greater than zero"
        )
        BigDecimal estimatedValue,

        @NotBlank(message = "Object description is required")
        @Size(
                min = 10,
                max = 2000,
                message = "Object description must contain between 10 and 2000 characters"
        )
        String objectDescription,

        @NotNull(message = "Opening date is required")
        LocalDate openingDate,

        @NotNull(message = "Closing date is required")
        LocalDate closingDate

) {
}
