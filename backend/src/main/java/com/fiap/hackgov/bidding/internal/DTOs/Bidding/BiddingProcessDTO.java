package com.fiap.hackgov.bidding.internal.DTOs.Bidding;

import com.fiap.hackgov.bidding.internal.DTOs.Edital.EditalDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.RequisitionResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Supplier.SupplierDTO;
import com.fiap.hackgov.bidding.internal.entities.enums.BiddingStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record BiddingProcessDTO(
        UUID id,
        RequisitionResponseDTO requisition,
        ProcessType type,
        Date legalDeadline,
        Date openingDate,
        EditalDTO edital,
        BiddingStatus status,
        UUID responsibleId,
        SupplierDTO winningSupplier,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
