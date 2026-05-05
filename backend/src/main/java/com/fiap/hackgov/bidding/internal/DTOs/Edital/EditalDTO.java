package com.fiap.hackgov.bidding.internal.DTOs.Edital;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record EditalDTO(

        UUID id,
        UUID biddingProcessId,
        Date openingDate,
        Date closingDate,
        Boolean impugn,
        String impugnReason,
        String documentUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}