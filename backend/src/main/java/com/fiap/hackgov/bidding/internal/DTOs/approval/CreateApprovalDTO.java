package com.fiap.hackgov.bidding.internal.DTOs.approval;

import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalSector;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateApprovalDTO(

        @NotNull(message = "Approval stage is required")
        ApprovalSector stage,

        @NotNull(message = "Approved by ID is required")
        UUID approvedById,

        @Size(max = 1000, message = "Observation must have at most 1000 characters")
        String observation

) {
}
