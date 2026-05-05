package com.fiap.hackgov.bidding.internal.DTOs.Approval;

import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateApprovalDTO(
        ApprovalStage stage,

        UUID approvedById,

        @Size(max = 1000, message = "Observation must have at most 1000 characters")
        String observation


) {
}
