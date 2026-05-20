package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.executionOrder.CreateExecutionOrderDTO;
import com.fiap.hackgov.bidding.internal.DTOs.executionOrder.ExecutionOrderResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.ExecutionOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExecutionOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "issuedBy", ignore = true)
    @Mapping(target = "commitments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ExecutionOrder toEntity(CreateExecutionOrderDTO dto);

    @Mapping(target = "contractId", source = "contract.id")
    @Mapping(target = "contractNumber", source = "contract.contractNumber")
    @Mapping(target = "issuedById", source = "issuedBy.id")
    @Mapping(target = "issuedByName", expression = "java(entity.getIssuedBy().getFullName())")
    ExecutionOrderResponseDTO toDTO(ExecutionOrder entity);

    List<ExecutionOrderResponseDTO> toDTOList(List<ExecutionOrder> entities);
}
