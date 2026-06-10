package com.fiap.hackgov.bidding.internal.DTOs.proporsal;

import com.fiap.hackgov.bidding.internal.entities.enums.ProposalImpugnationReason;
import com.fiap.hackgov.bidding.internal.entities.enums.ProposalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProposalResponseDTO(

        UUID id,

        UUID licitationProcessId,

        String licitationProcessNumber,

        UUID supplierId,

        String supplierName,

        String supplierCnpj,

        BigDecimal proposedValue,

        ProposalStatus status,

        ProposalImpugnationReason impugnationReason,

        String impugnationDetails,

        String observation,

        LocalDateTime submittedAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
