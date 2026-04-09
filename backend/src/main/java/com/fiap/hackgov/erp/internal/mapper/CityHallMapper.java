package com.fiap.hackgov.erp.internal.mapper;

import com.fiap.hackgov.erp.internal.DTOs.CityHall.CityHallDTO;
import com.fiap.hackgov.erp.internal.entities.CityHall;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CityHallMapper {

    @Mapping(source = "state.name", target = "stateName")
    CityHallDTO toCityHallDTO(CityHall cityHall);
}