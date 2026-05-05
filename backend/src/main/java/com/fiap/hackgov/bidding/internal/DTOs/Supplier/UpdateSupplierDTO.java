package com.fiap.hackgov.bidding.internal.DTOs.Supplier;

import jakarta.validation.constraints.*;

public record UpdateSupplierDTO(

        @Size(max = 200)
        String socialReason,

        @Pattern(regexp = "\\d{14}", message = "CNPJ must have exactly 14 digits")
        String cnpj,

        @Email
        @Size(max = 100)
        String email,

        @Size(max = 20)
        String phone,

        @Size(max = 100)
        String bank,

        @Size(max = 20)
        String bankAgency,

        @Size(max = 30)
        String bankAccount,

        Boolean isActive

) {}