package com.fiap.hackgov.cityhall_management.internal.DTOs.Sector;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSectorDTO(
        @NotBlank(message = "O nome do setor e obrigatorio")
        @Size(min = 3, max = 120, message = "O nome do setor deve ter entre 3 e 120 caracteres")
        String name,
        @Size(max = 140, message = "O identificador deve ter no maximo 140 caracteres")
        String slug,
        @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres")
        String description,
        Boolean active,
        CityHall cityHall
) {
}
