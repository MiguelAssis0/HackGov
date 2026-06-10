package com.fiap.hackgov.bidding.internal.DTOs.executionOrder;

import com.fiap.hackgov.bidding.internal.entities.enums.ExecutionOrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateRequisitionExecutionOrderDTO(

        @NotNull(message = "O tipo da ordem de execução é obrigatório")
        ExecutionOrderType type,

        @NotBlank(message = "O número da ordem de execução é obrigatório")
        @Size(max = 100, message = "O número da ordem de execução deve ter no máximo 100 caracteres")
        String number,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(min = 10, max = 2000, message = "A descrição deve conter entre 10 e 2000 caracteres")
        String description,

        @NotNull(message = "A data de emissão é obrigatória")
        LocalDate issuedAt

) {
}
