package com.fiap.hackgov.bidding.internal.DTOs.Approval;

import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalSector;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApprovalResponseDTO(

        UUID id,
        ApprovalSector stage,
        UUID approvedById,
        String observation,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}