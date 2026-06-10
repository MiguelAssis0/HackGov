package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.andress.AddressDTO;
import com.fiap.hackgov.bidding.internal.DTOs.supplier.CreateSupplierDTO;
import com.fiap.hackgov.bidding.internal.DTOs.supplier.SupplierResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.Address;
import com.fiap.hackgov.bidding.internal.entities.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "wonProcesses", ignore = true)
    @Mapping(target = "proposals", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Supplier toEntity(CreateSupplierDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "wonProcesses", ignore = true)
    @Mapping(target = "proposals", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CreateSupplierDTO dto, @MappingTarget Supplier entity);

    Address toEntity(AddressDTO dto);

    SupplierResponseDTO toDTO(Supplier entity);

    AddressDTO toDTO(Address entity);

    List<SupplierResponseDTO> toDTOList(List<Supplier> entities);
}
