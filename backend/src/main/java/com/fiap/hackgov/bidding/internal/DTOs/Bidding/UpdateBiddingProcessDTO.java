package com.fiap.hackgov.bidding.internal.DTOs.Bidding;

import com.fiap.hackgov.bidding.internal.entities.enums.BiddingStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.UUID;

public record UpdateBiddingProcessDTO(

        ProcessType type,

        @Future(message = "Legal deadline must be in the future")
        Date legalDeadline,

        @Future(message = "Opening date must be in the future")
        Date openingDate,

        UUID editalId,

        BiddingStatus status,

        UUID responsibleId,

        UUID winningSupplierId

) {}