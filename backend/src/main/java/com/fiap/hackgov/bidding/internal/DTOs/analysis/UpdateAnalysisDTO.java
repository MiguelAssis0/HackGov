package com.fiap.hackgov.bidding.internal.DTOs.analysis;

import com.fiap.hackgov.bidding.internal.entities.enums.AnalysisResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAnalysisDTO(

        @NotNull(message = "Analysis result is required")
        AnalysisResult result,

        @Size(
                min = 3,
                max = 1000,
                message = "Observation must contain between 3 and 1000 characters"
        )
        String observation

) {
}
