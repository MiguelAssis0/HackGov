package com.fiap.hackgov.cityhall_management.internal.mapper;

import com.fiap.hackgov.cityhall_management.internal.DTOs.State.StateDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.State;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StateMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StateDTO toStateDTO(State state);
}
