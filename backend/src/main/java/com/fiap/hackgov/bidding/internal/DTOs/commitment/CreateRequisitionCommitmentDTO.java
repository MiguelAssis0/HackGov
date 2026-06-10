package com.fiap.hackgov.bidding.internal.DTOs.commitment;

import com.fiap.hackgov.bidding.internal.entities.enums.CommitmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateRequisitionCommitmentDTO(

        @NotNull(message = "O tipo do empenho é obrigatório")
        CommitmentType type,

        @NotBlank(message = "O número do empenho é obrigatório")
        @Size(max = 100, message = "O número do empenho deve ter no máximo 100 caracteres")
        String commitmentNumber,

        @NotNull(message = "O valor reservado é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor reservado deve ser maior que zero")
        BigDecimal reservedValue

) {
}
