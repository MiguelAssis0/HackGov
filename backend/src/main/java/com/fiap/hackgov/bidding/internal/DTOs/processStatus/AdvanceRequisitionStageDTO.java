package com.fiap.hackgov.bidding.internal.DTOs.processStatus;

import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdvanceRequisitionStageDTO(

        @NotNull(message = "Proximo estágio não pode ser nulo")
        ProcessStage nextStage,

        @Size(min = 1, max = 255, message = "Observação deve ter entre 1 e 255 caracteres")
        String observation

) {
}
