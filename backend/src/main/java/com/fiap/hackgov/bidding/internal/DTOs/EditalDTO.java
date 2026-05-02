package com.fiap.hackgov.bidding.internal.DTOs;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record EditalDTO(
        UUID id,
        String number,
        String description,
        String object,
        Date publicationDate,
        Date deadlineDate,
        Date openingDate,
        String status,
        UUID responsibleId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
