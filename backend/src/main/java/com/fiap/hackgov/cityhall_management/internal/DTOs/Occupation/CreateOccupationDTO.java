package com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation;

import com.fiap.hackgov.cityhall_management.internal.entities.enums.LevelOccupation;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.TypeJobLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateOccupationDTO(
        @NotBlank(message = "O nome do cargo e obrigatorio")
        @Size(min = 3, max = 120, message = "O nome do cargo deve ter entre 3 e 120 caracteres")
        String name,
        @NotBlank(message = "A descricao do cargo e obrigatoria")
        @Size(min = 3, max = 500, message = "A descricao do cargo deve ter entre 3 e 500 caracteres")
        String description,
        @NotNull(message = "O tipo do cargo e obrigatorio")
        TypeJobLevel types,
        @NotNull(message = "O nivel do cargo e obrigatorio")
        LevelOccupation level,
        @NotNull(message = "O setor do cargo e obrigatorio")
        UUID sectorId
) {
}
