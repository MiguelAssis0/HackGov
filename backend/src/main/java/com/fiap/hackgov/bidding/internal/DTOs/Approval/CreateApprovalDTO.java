package com.fiap.hackgov.bidding.internal.DTOs.Approval;

import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStage;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateApprovalDTO(

        @NotNull(message = "Approval stage is required")
        ApprovalStage stage,

        @NotNull(message = "Approved by ID is required")
        UUID approvedById,

        @Size(max = 1000, message = "Observation must have at most 1000 characters")
        String observation

) {
}
