package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.proporsal.CreateProposalDTO;
import com.fiap.hackgov.bidding.internal.DTOs.proporsal.ProposalResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProposalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "licitationProcess", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "impugnationReason", ignore = true)
    @Mapping(target = "impugnationDetails", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Proposal toEntity(CreateProposalDTO dto);

    @Mapping(target = "licitationProcessId", source = "licitationProcess.id")
    @Mapping(target = "licitationProcessNumber", source = "licitationProcess.processNumber")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.tradeName")
    @Mapping(target = "supplierCnpj", source = "supplier.cnpj")
    ProposalResponseDTO toDTO(Proposal entity);

    List<ProposalResponseDTO> toDTOList(List<Proposal> entities);
}
