package com.fiap.hackgov.bidding.internal.DTOs.approval;

import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateApprovalDTO(

        @NotNull(message = "Approval status is required")
        ApprovalStatus status,

        @NotNull(message = "Approved by is required")
        UUID approvedBy,

        @Size(
                min = 3,
                max = 1000,
                message = "Observation must contain between 3 and 1000 characters"
        )
        String observation

) {
}
