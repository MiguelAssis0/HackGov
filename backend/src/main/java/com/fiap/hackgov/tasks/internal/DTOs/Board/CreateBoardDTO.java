package com.fiap.hackgov.tasks.internal.DTOs.Board;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;

public record CreateBoardDTO(
        String name,
        CityHall cityHall,
        Sector sector
) {
}
