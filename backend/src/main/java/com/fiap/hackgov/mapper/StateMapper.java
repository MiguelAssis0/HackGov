package com.fiap.hackgov.mapper;

import com.fiap.hackgov.DTOs.State.StateDTO;
import com.fiap.hackgov.entities.State;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StateMapper {

    StateDTO toStateDTO(State state);
}