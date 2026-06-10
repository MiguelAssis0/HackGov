package com.fiap.hackgov.bidding.internal.DTOs.accountabilityReport;

import com.fiap.hackgov.bidding.internal.entities.enums.AccountabilityStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAccountabilityReportDTO(

        @NotNull(message = "O id do contrato é obrigatório")
        UUID contractId,

        @NotNull(message = "O status da prestação de contas é obrigatório")
        AccountabilityStatus status,

        @Size(max = 2000, message = "A observação deve ter no máximo 2000 caracteres")
        String observation,

        @NotNull(message = "O id do responsável é obrigatório")
        UUID responsibleId,

        LocalDate analyzedAt
) {
}
