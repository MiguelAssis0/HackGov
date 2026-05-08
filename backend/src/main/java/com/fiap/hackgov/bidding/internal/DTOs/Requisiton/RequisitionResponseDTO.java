package com.fiap.hackgov.bidding.internal.DTOs.Requisiton;

import com.fiap.hackgov.bidding.internal.DTOs.Approval.ApprovalResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public record RequisitionResponseDTO(
        String number,
        String description,
        BigDecimal amount,
        Date requestDate,
        Date approvalDate,
        boolean requiresEtp,
        List<ApprovalResponseDTO> approvals,
        RequestStatus status,
        UUID requesterId,
        UUID approverId
) {
}
