package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.contract.ContractResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.contract.CreateContractDTO;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "licitationProcess", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "responsible", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Contract toEntity(CreateContractDTO dto);

    @Mapping(target = "licitationProcessId", source = "licitationProcess.id")
    @Mapping(target = "licitationProcessNumber", source = "licitationProcess.processNumber")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.corporateName")
    @Mapping(target = "responsibleId", source = "responsible.id")
    @Mapping(target = "responsibleName", expression = "java(entity.getResponsible().getFullName())")
    ContractResponseDTO toDTO(Contract entity);

    List<ContractResponseDTO> toDTOList(List<Contract> entities);
}
