package com.fiap.hackgov.bidding.internal.DTOs.requisiton;

import com.fiap.hackgov.bidding.internal.DTOs.etp.CreateETPDTO;
import com.fiap.hackgov.bidding.internal.entities.enums.AcquisitionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateRequisitionDTO(

        @NotNull(message = "Sector ID is required")
        UUID sectorId,

        @NotBlank(message = "Technical description is required")
        @Size(max = 5000,
                message = "Technical description must have at most 5000 characters")
        String technicalDescription,

        @NotBlank(message = "Justification is required")
        @Size(max = 5000,
                message = "Justification must have at most 5000 characters")
        String justification,

        @NotBlank(message = "Budget allocation is required")
        @Size(max = 255,
                message = "Budget allocation must have at most 255 characters")
        String budgetAllocation,

        @NotNull(message = "Acquisition type is required")
        AcquisitionType type,

        @Valid
        @NotNull(message = "ETP is required")
        CreateETPDTO etp

) {
}
