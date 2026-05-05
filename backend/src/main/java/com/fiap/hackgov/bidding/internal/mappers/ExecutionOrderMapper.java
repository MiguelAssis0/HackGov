package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.ExecutionOrder.*;
import com.fiap.hackgov.bidding.internal.entities.ExecutionOrder;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ExecutionOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "effort", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ExecutionOrder toEntity(CreateExecutionOrderDTO dto);

    ExecutionOrderDTO toDTO(ExecutionOrder entity);
}