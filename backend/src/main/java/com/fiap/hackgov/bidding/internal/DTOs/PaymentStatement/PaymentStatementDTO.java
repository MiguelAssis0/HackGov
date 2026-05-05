package com.fiap.hackgov.bidding.internal.DTOs.PaymentStatement;

import com.fiap.hackgov.bidding.internal.entities.enums.DeclarationType;
import com.fiap.hackgov.bidding.internal.entities.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record PaymentStatementDTO(

        UUID id,
        DeclarationType declarationType,
        UUID responsibleId,
        Boolean approved,
        BigDecimal amount,
        Date dueDate,
        Date paymentDate,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}