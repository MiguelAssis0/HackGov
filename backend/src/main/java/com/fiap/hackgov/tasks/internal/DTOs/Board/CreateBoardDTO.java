package com.fiap.hackgov.tasks.internal.DTOs.Board;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBoardDTO(
        @NotBlank(message = "O nome do quadro/setor e obrigatorio")
        @Size(min = 3, max = 120, message = "O nome do quadro/setor deve ter entre 3 e 120 caracteres")
        String name,
        @Valid
        @NotNull(message = "A prefeitura do quadro e obrigatoria")
        CityHall cityHall,
        @Valid
        @NotNull(message = "O setor vinculado ao quadro e obrigatorio")
        Sector sector
) {
    @AssertTrue(message = "A prefeitura informada deve conter um id valido")
    public boolean isCityHallValid() {
        return cityHall == null || cityHall.getId() != null;
    }

    @AssertTrue(message = "O setor informado deve conter um id valido")
    public boolean isSectorValid() {
        return sector == null || sector.getId() != null;
    }
}
