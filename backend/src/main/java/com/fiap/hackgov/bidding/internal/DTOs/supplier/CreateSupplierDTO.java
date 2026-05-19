package com.fiap.hackgov.bidding.internal.DTOs.supplier;

import com.fiap.hackgov.bidding.internal.DTOs.andress.AddressDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

public record CreateSupplierDTO(

        @CNPJ(message = "Invalid CNPJ")
        String cnpj,

        @NotBlank(message = "Corporate name is required")
        @Size(max = 255)
        String corporateName,

        @NotBlank(message = "Trade name is required")
        @Size(max = 255)
        String tradeName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,

        @NotBlank(message = "Phone is required")
        String phone,

        @NotBlank(message = "Legal representative is required")
        String legalRepresentative,

        @NotNull(message = "Address is required")
        AddressDTO address

) {
}