package com.fiap.hackgov.bidding.internal.DTOs;

public record CreateSupplierDTO(
        String socialReason,
        String cnpj,
        String email,
        String phone,
        String bank,
        String bankAgency,
        String bankAccount,
        Boolean isActive
) {
}
