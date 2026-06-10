package com.fiap.hackgov.bidding.internal.DTOs.requisiton;

import com.fiap.hackgov.bidding.internal.DTOs.etp.ETPDTO;
import com.fiap.hackgov.bidding.internal.entities.enums.AcquisitionType;
import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequisitionResponseDTO(

        UUID id,

        String registerNumber,

        RequisitionSectorDTO sector,

        RequisitionResponsibleDTO responsible,

        RequisitionResponsibleDTO procurementResponsible,

        String technicalDescription,

        String justification,

        String budgetAllocation,

        AcquisitionType type,

        RequisitionCurrentStageDTO currentStage,

        RequestStatus requestStatus,

        LocalDateTime finishedAt,

        ETPDTO etp,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
