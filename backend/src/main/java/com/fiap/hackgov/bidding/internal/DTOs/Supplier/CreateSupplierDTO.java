package com.fiap.hackgov.bidding.internal.DTOs.Supplier;

import jakarta.validation.constraints.*;

public record CreateSupplierDTO(

        @NotBlank(message = "Social reason is required")
        @Size(max = 200, message = "Social reason must have at most 200 characters")
        String socialReason,

        @NotBlank(message = "CNPJ is required")
        @Pattern(regexp = "\\d{14}", message = "CNPJ must have exactly 14 digits")
        String cnpj,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must have at most 100 characters")
        String email,

        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone must have at most 20 characters")
        String phone,

        @Size(max = 100, message = "Bank must have at most 100 characters")
        String bank,

        @Size(max = 20, message = "Bank agency must have at most 20 characters")
        String bankAgency,

        @Size(max = 30, message = "Bank account must have at most 30 characters")
        String bankAccount,

        @NotNull(message = "Active status is required")
        Boolean isActive

) {
}
