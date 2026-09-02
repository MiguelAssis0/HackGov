package com.fiap.hackgov.cityhall_management.internal.mapper;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation.CreateOccupationDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation.OccupationResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OccupationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sectorId", ignore = true)
    @Mapping(target = "cityHall", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Occupation toEntity(CreateOccupationDTO dto);

    @Mapping(target = "sectorId", source = "sectorId.id")
    @Mapping(target = "sector", source = "sectorId.name")
    OccupationResponseDTO toDTO(Occupation occupation);
}
