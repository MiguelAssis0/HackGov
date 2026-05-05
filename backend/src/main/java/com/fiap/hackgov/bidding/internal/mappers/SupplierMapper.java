package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.Supplier.*;
import com.fiap.hackgov.bidding.internal.entities.Supplier;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "biddingProcesses", ignore = true)
    @Mapping(target = "contracts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Supplier toEntity(CreateSupplierDTO dto);

    @Mapping(target = "biddingProcesses", ignore = true)
    @Mapping(target = "contracts", ignore = true)
    SupplierDTO toDTO(Supplier entity);
}