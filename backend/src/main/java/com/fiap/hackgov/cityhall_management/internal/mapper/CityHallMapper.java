package com.fiap.hackgov.cityhall_management.internal.mapper;

import com.fiap.hackgov.cityhall_management.internal.DTOs.CityHall.CityHallDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CityHallMapper {

    @Mapping(source = "state.name", target = "stateName")
    CityHallDTO toCityHallDTO(CityHall cityHall);
}