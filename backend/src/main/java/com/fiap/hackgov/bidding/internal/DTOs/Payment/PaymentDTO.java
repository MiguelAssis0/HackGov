package com.fiap.hackgov.bidding.internal.DTOs.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record PaymentDTO(

        UUID id,
        BigDecimal valuePayment,
        Boolean approved,
        UUID sectorId,
        UUID approvedById,
        Date paymentDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}