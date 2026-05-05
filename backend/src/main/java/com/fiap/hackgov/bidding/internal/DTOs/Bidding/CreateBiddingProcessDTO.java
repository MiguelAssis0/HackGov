package com.fiap.hackgov.bidding.internal.DTOs.Bidding;

import com.fiap.hackgov.bidding.internal.entities.enums.BiddingStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessType;
import jakarta.validation.constraints.*;

import java.util.Date;
import java.util.UUID;

public record CreateBiddingProcessDTO(

        @NotNull(message = "Requisition ID is required")
        UUID requisitionId,

        @NotNull(message = "Process type is required")
        ProcessType type,

        @NotNull(message = "Legal deadline is required")
        @Future(message = "Legal deadline must be in the future")
        Date legalDeadline,

        @NotNull(message = "Opening date is required")
        @Future(message = "Opening date must be in the future")
        Date openingDate,

        UUID editalId,

        @NotNull(message = "Status is required")
        BiddingStatus status,

        @NotNull(message = "Responsible ID is required")
        UUID responsibleId,

        UUID winningSupplierId

) {
}
