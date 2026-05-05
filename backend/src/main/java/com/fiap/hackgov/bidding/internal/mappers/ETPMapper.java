package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.ETP.*;
import com.fiap.hackgov.bidding.internal.entities.ETP;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ETPMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requisition", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ETP toEntity(CreateETPDTO dto);

    @Mapping(source = "requisition.id", target = "requisitionId")
    ETPDTO toDTO(ETP entity);

    CreateETPDTO toCreateETPDTO(ETP entity);
}