package com.fiap.hackgov.bidding.internal.DTOs.requisiton;

import com.fiap.hackgov.bidding.internal.DTOs.etp.ETPDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequisitionResponseDTO(

        UUID id,

        String registerNumber,

        RequisitionSectorDTO sector,

        RequisitionResponsibleDTO responsible,

        String technicalDescription,

        String justification,

        String budgetAllocation,

        RequisitionCurrentStageDTO currentStage,

        ETPDTO etp,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
