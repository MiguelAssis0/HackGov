package com.fiap.hackgov.bidding.internal.DTOs.ETP;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateETPDTO(

        @NotNull(message = "Requisition ID is required")
        UUID requisitionId,

        @NotBlank(message = "Content is required")
        @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
        String content

) {}