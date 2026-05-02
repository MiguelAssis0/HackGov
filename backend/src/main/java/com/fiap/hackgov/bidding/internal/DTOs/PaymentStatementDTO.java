package com.fiap.hackgov.bidding.internal.DTOs;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record PaymentStatementDTO(
        UUID id,
        BigDecimal amount,
        Date dueDate,
        Date paymentDate,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
