package com.fiap.hackgov.bidding.internal.DTOs.etp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateETPDTO(

        @NotBlank(message = "Content is required")
        @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
        String content

) {
}