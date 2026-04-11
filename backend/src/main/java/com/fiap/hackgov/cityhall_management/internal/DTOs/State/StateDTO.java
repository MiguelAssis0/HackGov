package com.fiap.hackgov.cityhall_management.internal.DTOs.State;

import com.fiap.hackgov.cityhall_management.internal.entities.enums.UF;

import java.time.LocalDateTime;
import java.util.UUID;

public record StateDTO(
        UUID id,
        String name,
        UF uf,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}