package com.fiap.hackgov.erp.internal.DTOs.CityHall;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

import java.util.UUID;

public record CreateCityHallDTO(

        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @NotBlank(message = "CNPJ is required")
        @CNPJ
        String cnpj,

        @NotNull(message = "State ID is required")
        UUID stateId
) {}