package com.fiap.hackgov.bidding.internal.DTOs.licitation;

import com.fiap.hackgov.bidding.internal.DTOs.supplier.CreateSupplierDTO;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublishLicitationResultDTO(

        @NotNull(message = "Status is required")
        LicitationStatus status,

        @NotBlank(message = "Observation is required")
        @Size(min = 5, max = 1000, message = "Observation must contain between 5 and 1000 characters")
        String observation,

        @Valid
        CreateSupplierDTO winnerSupplier

) {
}
