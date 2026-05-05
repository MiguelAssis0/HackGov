package com.fiap.hackgov.bidding.internal.DTOs.ETP;

import jakarta.validation.constraints.Size;

public record UpdateETPDTO(

        @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
        String content

) {}