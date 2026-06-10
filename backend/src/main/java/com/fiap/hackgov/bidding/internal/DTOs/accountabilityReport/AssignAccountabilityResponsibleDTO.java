package com.fiap.hackgov.bidding.internal.DTOs.accountabilityReport;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AssignAccountabilityResponsibleDTO(

        @NotNull(message = "O id do responsável é obrigatório")
        UUID employeeId,

        @Size(max = 2000, message = "A observação deve ter no máximo 2000 caracteres")
        String observation
) {
}
