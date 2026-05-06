package com.fiap.hackgov.bidding.internal.DTOs.Requisiton;

import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record CreateRequisitionDTO(

        @NotBlank(message = "Technical description is required")
        @Size(max = 255, message = "Technical description  must have at most 255 characters")
        String technicalDescription,

        @NotNull(message = "Budget allocation is required")
        @DecimalMin(value = "0.01", message = "Budget allocation must be greater than zero")
        BigDecimal budgetAllocation,

        @NotNull(message = "Requires ETP is required")
        boolean requiresEtp,

        @NotNull(message = "Request date is required")
        @PastOrPresent(message = "Request date cannot be in the future")
        Date requestDate,

        @PastOrPresent(message = "Approval date cannot be in the future")
        Date approvalDate,

        @NotNull(message = "Status is required")
        RequestStatus status,

        @NotNull(message = "Requester ID is required")
        UUID requesterId,

        UUID approverId

) {
}
