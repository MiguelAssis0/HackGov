package com.fiap.hackgov.bidding.internal.DTOs.Edital;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.UUID;

public record UpdateEditalDTO(

        UUID biddingProcessId,

        @Future(message = "Opening date must be in the future")
        Date openingDate,

        @Future(message = "Closing date must be in the future")
        Date closingDate,

        Boolean impugn,

        @Size(max = 1000)
        String impugnReason,

        @Size(max = 500)
        String documentUrl

) {}