package com.fiap.hackgov.bidding.internal.DTOs.proporsal;

import com.fiap.hackgov.bidding.internal.entities.enums.ProposalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProposalStatusDTO(

        @NotNull(message = "Status is required")
        ProposalStatus status,

        @Size(
                max = 1000,
                message = "Observation must contain at most 1000 characters"
        )
        String observation

) {
}
