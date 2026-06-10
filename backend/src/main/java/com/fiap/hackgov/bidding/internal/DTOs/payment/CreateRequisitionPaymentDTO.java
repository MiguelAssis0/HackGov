package com.fiap.hackgov.bidding.internal.DTOs.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateRequisitionPaymentDTO(

        @NotNull(message = "O valor do pagamento é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor do pagamento deve ser maior que zero")
        BigDecimal value,

        @NotNull(message = "A data de pagamento é obrigatória")
        @PastOrPresent(message = "A data de pagamento não pode ser futura")
        LocalDate paidAt
) {
}
