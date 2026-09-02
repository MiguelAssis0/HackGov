package com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation;

import com.fiap.hackgov.cityhall_management.internal.entities.enums.LevelOccupation;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.TypeJobLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateOccupationDTO(
        @NotBlank(message = "O nome do cargo e obrigatorio")
        @Size(min = 3, max = 120, message = "O nome do cargo deve ter entre 3 e 120 caracteres")
        String name,
        @Size(max = 140, message = "O identificador deve ter no maximo 140 caracteres")
        String slug,
        @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
        String description,
        TypeJobLevel types,
        LevelOccupation level,
        UUID sectorId,
        Boolean active
) {
}
