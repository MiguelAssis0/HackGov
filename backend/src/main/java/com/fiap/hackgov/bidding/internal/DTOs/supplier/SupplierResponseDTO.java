package com.fiap.hackgov.bidding.internal.DTOs.supplier;

import com.fiap.hackgov.bidding.internal.DTOs.andress.AddressDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierResponseDTO(

        UUID id,

        String cnpj,

        String corporateName,

        String tradeName,

        String email,

        String phone,

        String legalRepresentative,

        Boolean active,

        AddressDTO address,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}