package com.fiap.hackgov.erp.internal.DTOs.CityHall;

import java.time.LocalDateTime;
import java.util.UUID;

public record CityHallDTO(
        UUID id,
        String name,
        String cnpj,
        String stateName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
