package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.Supplier.*;
import com.fiap.hackgov.bidding.internal.entities.Supplier;
import com.fiap.hackgov.bidding.internal.mappers.SupplierMapper;
import com.fiap.hackgov.bidding.internal.repositories.SupplierRepository;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository repository;
    private final SupplierMapper mapper;

    public SupplierDTO create(CreateSupplierDTO dto) {

        if (repository.existsByCnpj(dto.cnpj())) {
            throw new IllegalArgumentException("Supplier with this CNPJ already exists");
        }

        Supplier entity = mapper.toEntity(dto);

        return mapper.toDTO(repository.save(entity));
    }

    public Page<SupplierDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public SupplierDTO findById(UUID id) {
        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"))
        );
    }

    public SupplierDTO update(UUID id, UpdateSupplierDTO dto) {
        Supplier entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        if (dto.socialReason() != null) entity.setSocialReason(dto.socialReason());

        if (dto.cnpj() != null && !dto.cnpj().equals(entity.getCnpj())) {
            if (repository.existsByCnpj(dto.cnpj())) {
                throw new IllegalArgumentException("CNPJ already in use");
            }
            entity.setCnpj(dto.cnpj());
        }

        if (dto.email() != null) entity.setEmail(dto.email());
        if (dto.phone() != null) entity.setPhone(dto.phone());
        if (dto.bank() != null) entity.setBank(dto.bank());
        if (dto.bankAgency() != null) entity.setBankAgency(dto.bankAgency());
        if (dto.bankAccount() != null) entity.setBankAccount(dto.bankAccount());
        if (dto.isActive() != null) entity.setIsActive(dto.isActive());

        return mapper.toDTO(repository.save(entity));
    }

    public void delete(UUID id) {
        Supplier entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        repository.delete(entity);
    }
}