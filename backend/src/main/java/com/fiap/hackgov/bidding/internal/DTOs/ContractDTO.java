package com.fiap.hackgov.bidding.internal.DTOs;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record ContractDTO(
        UUID id,
        String number,
        String object,
        BigDecimal amount,
        Date assignedDate,
        Date startDate,
        Date endDate,
        UUID responsibleId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        BiddingProcessDTO biddingProcess,
        SupplierDTO supplier,
        ExecutionOrderDTO executionOrder,
        EffortDTO effort
) {
}
