package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.commitment.CommitmentResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.commitment.CreateCommitmentDTO;
import com.fiap.hackgov.bidding.internal.entities.Commitment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommitmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "executionOrder", ignore = true)
    @Mapping(target = "issuedBy", ignore = true)
    @Mapping(target = "paymentDeclarations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Commitment toEntity(CreateCommitmentDTO dto);

    @Mapping(target = "contractId", source = "contract.id")
    @Mapping(target = "contractNumber", source = "contract.contractNumber")
    @Mapping(target = "executionOrderId", source = "executionOrder.id")
    @Mapping(target = "executionOrderNumber", source = "executionOrder.number")
    @Mapping(target = "issuedById", source = "issuedBy.id")
    @Mapping(target = "issuedByName", expression = "java(entity.getIssuedBy().getFullName())")
    CommitmentResponseDTO toDTO(Commitment entity);

    List<CommitmentResponseDTO> toDTOList(List<Commitment> entities);
}
