package com.fiap.hackgov.cityhall_management.internal.mapper;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.CreateSectorDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.SectorResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SectorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "occupationId", ignore = true)
    @Mapping(target = "sectorTools", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Sector toEntity(CreateSectorDTO createSectorDTO);

    SectorResponseDTO toDTO(Sector sector);
}
