package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.analysis.AnalysisResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.analysis.CreateAnalysisDTO;
import com.fiap.hackgov.bidding.internal.entities.Analysis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalysisMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requisition", ignore = true)
    @Mapping(target = "result", ignore = true)
    @Mapping(target = "analyzedBy", ignore = true)
    @Mapping(target = "analyzedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Analysis toEntity(CreateAnalysisDTO dto);

    @Mapping(target = "requisitionId", source = "requisition.id")
    @Mapping(target = "analyzedById", source = "analyzedBy.id")
    AnalysisResponseDTO toDTO(Analysis analysis);
}
