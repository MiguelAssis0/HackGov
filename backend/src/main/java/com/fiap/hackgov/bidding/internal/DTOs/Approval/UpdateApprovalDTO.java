package com.fiap.hackgov.bidding.internal.DTOs.Approval;

import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalSector;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateApprovalDTO(
        ApprovalSector stage,

        UUID approvedById,

        @Size(max = 1000, message = "Observation must have at most 1000 characters")
        String observation


) {
}
