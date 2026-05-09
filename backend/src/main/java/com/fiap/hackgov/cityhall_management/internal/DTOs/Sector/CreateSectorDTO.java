package com.fiap.hackgov.cityhall_management.internal.DTOs.Sector;


import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;


public record CreateSectorDTO(
        String name,
        CityHall cityHall
) {
}
