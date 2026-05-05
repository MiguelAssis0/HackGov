package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.ProcessState.*;
import com.fiap.hackgov.bidding.internal.entities.ProcessState;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProcessStateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "startedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "biddingProcess", source = "biddingProcessId", qualifiedByName = "mapToRequisition")
    ProcessState toEntity(CreateProcessStateDTO dto);

    @Mapping(target = "biddingProcessId", source = "biddingProcess.id")
    ProcessStateResponseDTO toDTO(ProcessState entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateProcessStateDTO dto, @MappingTarget ProcessState entity);

    @Named("mapToRequisition")
    default Requisition mapToRequisition(UUID id) {
        if (id == null) return null;
        Requisition req = new Requisition();
        req.setId(id);
        return req;
    }
}