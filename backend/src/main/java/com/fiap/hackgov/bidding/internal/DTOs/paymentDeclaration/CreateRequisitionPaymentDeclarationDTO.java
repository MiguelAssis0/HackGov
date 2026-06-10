package com.fiap.hackgov.bidding.internal.DTOs.paymentDeclaration;

import com.fiap.hackgov.bidding.internal.entities.enums.PaymentDeclarationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRequisitionPaymentDeclarationDTO(

        @NotNull(message = "O tipo da declaração é obrigatório")
        PaymentDeclarationType type,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(min = 10, max = 2000, message = "A descrição deve conter entre 10 e 2000 caracteres")
        String description

) {
}
