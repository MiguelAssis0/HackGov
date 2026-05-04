package com.fiap.hackgov.bidding.internal.DTOs.Requisiton;

import com.fiap.hackgov.bidding.internal.entities.Approval;
import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public record RequisitionDTO(
        String number,
        String description,
        BigDecimal amount,
        Date requestDate,
        Date approvalDate,
        boolean requiresEtp,
        List<Approval> approvals,
        RequestStatus status,
        UUID requesterId,
        UUID approverId
) {
}
