package com.fiap.hackgov.bidding.internal.DTOs.ETP;

import java.time.LocalDateTime;
import java.util.UUID;

public record ETPDTO(

        UUID id,
        UUID requisitionId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}