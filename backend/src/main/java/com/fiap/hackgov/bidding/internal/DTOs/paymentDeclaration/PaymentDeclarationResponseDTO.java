package com.fiap.hackgov.bidding.internal.DTOs.paymentDeclaration;

import com.fiap.hackgov.bidding.internal.entities.enums.PaymentDeclarationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDeclarationResponseDTO(
        UUID id,
        UUID commitmentId,
        String commitmentNumber,
        UUID contractId,
        String contractNumber,
        PaymentDeclarationType type,
        String description,
        UUID approvedById,
        String approvedByName,
        Boolean secretaryApproved,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
