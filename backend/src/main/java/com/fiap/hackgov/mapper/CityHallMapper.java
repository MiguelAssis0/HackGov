package com.fiap.hackgov.mapper;

import com.fiap.hackgov.DTOs.CityHall.CityHallDTO;
import com.fiap.hackgov.entities.CityHall;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CityHallMapper {

    @Mapping(source = "state.name", target = "stateName")
    CityHallDTO toCityHallDTO(CityHall cityHall);
}