package com.fiap.hackgov.bidding.internal.DTOs.Approval;

import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStage;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApprovalResponseDTO(

        UUID id,
        ApprovalStage stage,
        UUID approvedById,
        String observation,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}