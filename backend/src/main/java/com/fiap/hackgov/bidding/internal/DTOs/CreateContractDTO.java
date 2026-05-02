package com.fiap.hackgov.bidding.internal.DTOs;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record CreateContractDTO(
        String number,
        String object,
        BigDecimal amount,
        Date assignedDate,
        Date startDate,
        Date endDate,
        UUID responsibleId,
        UUID biddingProcessId,
        UUID supplierId,
        UUID executionOrderId,
        UUID effortId
) {
}
