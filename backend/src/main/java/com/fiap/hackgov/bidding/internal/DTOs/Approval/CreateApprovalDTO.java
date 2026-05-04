package com.fiap.hackgov.bidding.internal.DTOs.Approval;


import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateApprovalDTO(

        @NotNull(message = "Approval stage is required")
        ApprovalStage stage,

        @NotNull(message = "Approved by id is required")
        UUID approvedById,

        @NotBlank(message = "Observation is required")
        String observation

) {
}
