package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.Effort.*;
import com.fiap.hackgov.bidding.internal.entities.Effort;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EffortMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "executionOrder", ignore = true)
    @Mapping(target = "paymentStatement", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Effort toEntity(CreateEffortDTO dto);

    EffortDTO toDTO(Effort entity);
}