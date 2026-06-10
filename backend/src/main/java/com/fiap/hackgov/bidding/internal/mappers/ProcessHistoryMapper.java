package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.processHistory.ProcessHistoryDTO;
import com.fiap.hackgov.bidding.internal.entities.ProcessHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProcessHistoryMapper {

    @Mapping(source = "changedBy.id", target = "changedById")
    @Mapping(target = "changedByName", expression = "java(mapName(entity))")
    ProcessHistoryDTO toDTO(ProcessHistory entity);

    List<ProcessHistoryDTO> toDTOList(List<ProcessHistory> list);

    default String mapName(ProcessHistory entity) {

        if (entity.getChangedBy() == null) return null;

        return entity.getChangedBy().getFirstName() + " " + entity.getChangedBy().getLastName();
    }
}