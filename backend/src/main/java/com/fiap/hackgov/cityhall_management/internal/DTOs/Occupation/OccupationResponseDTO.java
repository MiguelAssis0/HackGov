package com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation;

import com.fiap.hackgov.cityhall_management.internal.entities.enums.LevelOccupation;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.TypeJobLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public record OccupationResponseDTO(
        UUID id,
        String name,
        String slug,
        String description,
        TypeJobLevel types,
        LevelOccupation level,
        UUID sectorId,
        String sector,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
