package com.fiap.hackgov.erp.internal.mapper;

import com.fiap.hackgov.erp.internal.DTOs.State.StateDTO;
import com.fiap.hackgov.erp.internal.entities.State;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StateMapper {

    StateDTO toStateDTO(State state);
}