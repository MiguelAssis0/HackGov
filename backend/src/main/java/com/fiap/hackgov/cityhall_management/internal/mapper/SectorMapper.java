package com.fiap.hackgov.cityhall_management.internal.mapper;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.CreateSectorDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.SectorResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SectorMapper {

    Sector toEntity(CreateSectorDTO createSectorDTO);
    SectorResponseDTO toDTO(Sector sector);
}
