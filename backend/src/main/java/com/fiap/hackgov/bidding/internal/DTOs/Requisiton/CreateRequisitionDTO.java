package com.fiap.hackgov.bidding.internal.DTOs.Requisiton;

import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record CreateRequisitionDTO(

        @NotBlank(message = "Number is required")
        @Size(max = 50, message = "Number must have at most 50 characters")
        String number,

        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description must have at most 255 characters")
        String description,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

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
