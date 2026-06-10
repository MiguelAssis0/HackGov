package com.fiap.hackgov.bidding.internal.DTOs.contract;

import com.fiap.hackgov.bidding.internal.entities.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContractResponseDTO(

        UUID id,

        UUID licitationProcessId,

        String licitationProcessNumber,

        UUID supplierId,

        String supplierName,

        String contractNumber,

        String objectDescription,

        BigDecimal totalValue,

        LocalDate signedAt,

        LocalDate startDate,

        LocalDate endDate,

        UUID responsibleId,

        String responsibleName,

        ContractStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
