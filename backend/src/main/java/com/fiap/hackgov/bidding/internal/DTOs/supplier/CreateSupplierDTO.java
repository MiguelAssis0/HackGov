package com.fiap.hackgov.bidding.internal.DTOs.supplier;

import com.fiap.hackgov.bidding.internal.DTOs.andress.AddressDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

public record CreateSupplierDTO(

        @CNPJ(message = "Invalid CNPJ")
        @NotBlank(message = "CNPJ is required")
        @Pattern(
                regexp = "^(?:\\d{14}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})$",
                message = "Invalid CNPJ format"
        )
        String cnpj,

        @NotBlank(message = "Corporate name is required")
        @Size(min = 2, max = 255)
        @Pattern(regexp = "^[\\p{L}\\p{N} .,&'()/-]+$", message = "Invalid corporate name")
        String corporateName,

        @NotBlank(message = "Trade name is required")
        @Size(min = 2, max = 255)
        @Pattern(regexp = "^[\\p{L}\\p{N} .,&'()/-]+$", message = "Invalid trade name")
        String tradeName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(
                regexp = "^(?:\\(\\d{2}\\)\\s?(?:9\\d{4}|\\d{4})-\\d{4}|\\d{10,11})$",
                message = "Invalid phone"
        )
        String phone,

        @NotBlank(message = "Legal representative is required")
        @Size(min = 3, max = 255)
        @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Invalid legal representative")
        String legalRepresentative,

        @NotNull(message = "Address is required")
        AddressDTO address

) {
}
