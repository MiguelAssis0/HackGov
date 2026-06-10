package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.licitation.LicitationHistoryDTO;
import com.fiap.hackgov.bidding.internal.entities.LicitationHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LicitationHistoryMapper {

    @Mapping(target = "changedById", source = "changedBy.id")
    @Mapping(target = "changedByName", expression = "java(entity.getChangedBy().getFullName())")
    LicitationHistoryDTO toDTO(LicitationHistory entity);

    List<LicitationHistoryDTO> toDTOList(List<LicitationHistory> entities);
}