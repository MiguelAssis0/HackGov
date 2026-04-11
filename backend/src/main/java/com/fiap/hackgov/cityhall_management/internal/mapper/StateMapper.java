package com.fiap.hackgov.cityhall_management.internal.mapper;

import com.fiap.hackgov.cityhall_management.internal.DTOs.State.StateDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.State;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StateMapper {

    StateDTO toStateDTO(State state);
}