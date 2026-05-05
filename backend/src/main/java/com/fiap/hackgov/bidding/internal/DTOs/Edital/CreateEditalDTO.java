package com.fiap.hackgov.bidding.internal.DTOs.Edital;

import jakarta.validation.constraints.*;

import java.util.Date;
import java.util.UUID;

public record CreateEditalDTO(

        @NotNull(message = "Bidding process ID is required")
        UUID biddingProcessId,

        @NotNull(message = "Opening date is required")
        @Future(message = "Opening date must be in the future")
        Date openingDate,

        @NotNull(message = "Closing date is required")
        @Future(message = "Closing date must be in the future")
        Date closingDate,

        Boolean impugn,

        @Size(max = 1000, message = "Impugn reason must have at most 1000 characters")
        String impugnReason,

        @NotBlank(message = "Document URL is required")
        @Size(max = 500)
        String documentUrl

) {}