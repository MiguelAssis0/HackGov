package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.licitation.CreateLicitationProcessDTO;
import com.fiap.hackgov.bidding.internal.DTOs.licitation.LicitationProcessResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LicitationProcessMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processNumber", ignore = true)
    @Mapping(target = "requisition", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "winnerSupplier", ignore = true)
    @Mapping(target = "notice", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "proposals", ignore = true)
    @Mapping(target = "histories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LicitationProcess toEntity(CreateLicitationProcessDTO dto);

    @Mapping(target = "requisitionId", source = "requisition.id")
    @Mapping(target = "requisitionNumber", source = "requisition.registerNumber")
    LicitationProcessResponseDTO toDTO(LicitationProcess entity);

    List<LicitationProcessResponseDTO> toDTOList(List<LicitationProcess> entities);
}
