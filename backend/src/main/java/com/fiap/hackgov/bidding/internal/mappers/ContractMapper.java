package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.Contract.*;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "biddingProcess", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "executionOrder", ignore = true)
    @Mapping(target = "effort", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Contract toEntity(CreateContractDTO dto);

    ContractDTO toDTO(Contract entity);
}