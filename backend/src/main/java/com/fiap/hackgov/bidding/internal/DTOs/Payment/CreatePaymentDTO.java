package com.fiap.hackgov.bidding.internal.DTOs.Payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record CreatePaymentDTO(

        @NotNull(message = "Value is required")
        @Positive(message = "Value must be greater than zero")
        BigDecimal valuePayment,

        @NotNull(message = "Sector is required")
        UUID sectorId,

        UUID approvedById,

        @NotNull(message = "Payment date is required")
        Date paymentDate

) {}