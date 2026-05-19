package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.andress.AddressDTO;
import com.fiap.hackgov.bidding.internal.DTOs.supplier.CreateSupplierDTO;
import com.fiap.hackgov.bidding.internal.DTOs.supplier.SupplierResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.Address;
import com.fiap.hackgov.bidding.internal.entities.Supplier;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    Supplier toEntity(CreateSupplierDTO dto);

    Address toEntity(AddressDTO dto);

    SupplierResponseDTO toDTO(Supplier entity);

    AddressDTO toDTO(Address entity);

    List<SupplierResponseDTO> toDTOList(List<Supplier> entities);
}