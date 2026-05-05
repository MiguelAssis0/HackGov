package com.fiap.hackgov.bidding.internal.DTOs.PaymentStatement;

import com.fiap.hackgov.bidding.internal.entities.enums.DeclarationType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record CreatePaymentStatementDTO(

        @NotNull
        DeclarationType declarationType,

        @NotNull
        UUID responsibleId,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @NotNull
        @Future
        Date dueDate,

        @NotNull
        UUID effortId

) {}