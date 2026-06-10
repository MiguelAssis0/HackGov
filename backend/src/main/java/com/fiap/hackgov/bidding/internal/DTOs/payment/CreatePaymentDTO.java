package com.fiap.hackgov.bidding.internal.DTOs.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePaymentDTO(

        @NotNull(message = "O id da declaração é obrigatório")
        UUID declarationId,

        @NotNull(message = "O valor do pagamento é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor do pagamento deve ser maior que zero")
        BigDecimal value,

        @NotNull(message = "A aprovação da fazenda é obrigatória")
        Boolean treasuryApproved,

        @NotNull(message = "O id do responsável da fazenda é obrigatório")
        UUID treasuryResponsibleId,

        @NotNull(message = "O id do setor da fazenda é obrigatório")
        UUID treasurySectorId,

        @NotNull(message = "O id do aprovador é obrigatório")
        UUID approvedById,

        @NotNull(message = "A data de pagamento é obrigatória")
        LocalDate paidAt
) {
}
