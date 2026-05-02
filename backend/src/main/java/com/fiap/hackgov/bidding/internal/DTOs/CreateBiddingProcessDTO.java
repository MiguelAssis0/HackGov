package com.fiap.hackgov.bidding.internal.DTOs;

import com.fiap.hackgov.bidding.internal.entities.enums.BiddingStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessType;

import java.util.Date;
import java.util.UUID;

public record CreateBiddingProcessDTO(
        UUID requisitionId,
        ProcessType type,
        Date legalDeadline,
        Date openingDate,
        UUID editalId,
        BiddingStatus status,
        UUID responsibleId,
        UUID winningSupplierId
) {
}
