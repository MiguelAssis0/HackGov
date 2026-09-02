package com.fiap.hackgov.cityhall_management.internal.DTOs.Sector;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;

import java.util.UUID;

public record SectorResponseDTO(
        UUID id,
        String name,
        String slug,
        String description,
        boolean active,
        CityHall cityHall
) {
}
