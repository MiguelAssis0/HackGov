package com.fiap.hackgov.bidding.internal.DTOs.Contract;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record UpdateContractDTO(

        @Size(max = 50, message = "Contract number must have at most 50 characters")
        String number,

        @Size(max = 1000, message = "Contract object must have at most 1000 characters")
        String object,

        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer digits and 2 decimal digits")
        BigDecimal amount,

        @PastOrPresent(message = "Assigned date cannot be in the future")
        Date assignedDate,

        @Future(message = "Start date must be in the future")
        Date startDate,

        @Future(message = "End date must be in the future")
        Date endDate,

        UUID responsibleId,
        UUID biddingProcessId,
        UUID supplierId,
        UUID executionOrderId,
        UUID effortId

) {}