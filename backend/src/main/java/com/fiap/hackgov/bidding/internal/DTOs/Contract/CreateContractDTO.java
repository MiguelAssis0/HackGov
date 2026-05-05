package com.fiap.hackgov.bidding.internal.DTOs.Contract;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record CreateContractDTO(

        @NotBlank(message = "Contract number is required")
        @Size(max = 50, message = "Contract number must have at most 50 characters")
        String number,

        @NotBlank(message = "Contract object is required")
        @Size(max = 1000, message = "Contract object must have at most 1000 characters")
        String object,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer digits and 2 decimal digits")
        BigDecimal amount,

        @NotNull(message = "Assigned date is required")
        @PastOrPresent(message = "Assigned date cannot be in the future")
        Date assignedDate,

        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        Date startDate,

        @Future(message = "End date must be in the future")
        Date endDate,

        @NotNull(message = "Responsible ID is required")
        UUID responsibleId,

        @NotNull(message = "Bidding process ID is required")
        UUID biddingProcessId,

        @NotNull(message = "Supplier ID is required")
        UUID supplierId,

        UUID executionOrderId,

        UUID effortId

) {
}
