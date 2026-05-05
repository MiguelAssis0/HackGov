package com.fiap.hackgov.bidding.internal.DTOs.Payment;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record UpdatePaymentDTO(

        BigDecimal valuePayment,
        Boolean approved,
        UUID approvedById,
        Date paymentDate,
        UUID sectorId

) {}