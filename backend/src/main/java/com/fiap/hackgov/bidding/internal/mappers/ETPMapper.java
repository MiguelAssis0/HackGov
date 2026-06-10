package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.etp.CreateETPDTO;
import com.fiap.hackgov.bidding.internal.DTOs.etp.ETPDTO;
import com.fiap.hackgov.bidding.internal.entities.ETP;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ETPMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requisition", ignore = true)
    ETP toEntity(CreateETPDTO dto);

    @Mapping(source = "requisition.id", target = "requisitionId")
    ETPDTO toDTO(ETP entity);

}