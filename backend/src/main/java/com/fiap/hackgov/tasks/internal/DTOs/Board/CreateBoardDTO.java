package com.fiap.hackgov.tasks.internal.DTOs.Board;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;

import java.util.UUID;

public record CreateBoardDTO(
        String name,
        CityHall cityHall,
        Sector sector
) {
}
