package com.fiap.hackgov.bidding.internal.DTOs.PaymentStatement;

import com.fiap.hackgov.bidding.internal.entities.enums.DeclarationType;
import com.fiap.hackgov.bidding.internal.entities.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record UpdatePaymentStatementDTO(

        DeclarationType declarationType,
        UUID responsibleId,
        BigDecimal amount,
        Date dueDate,
        Date paymentDate,
        PaymentStatus status,
        Boolean approved,
        UUID effortId

) {}