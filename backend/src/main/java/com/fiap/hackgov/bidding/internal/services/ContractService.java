package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.Contract.*;
import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.mappers.ContractMapper;
import com.fiap.hackgov.bidding.internal.repositories.*;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {

    private final ContractRepository repository;
    private final ContractMapper mapper;

    private final BiddingProcessRepository biddingRepository;
    private final SupplierRepository supplierRepository;
    private final ExecutionOrderRepository executionOrderRepository;
    private final EffortRepository effortRepository;

    public ContractDTO create(CreateContractDTO dto) {

        if (dto.endDate() != null && dto.endDate().before(dto.startDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Contract entity = mapper.toEntity(dto);

        entity.setBiddingProcess(
                biddingRepository.findById(dto.biddingProcessId())
                        .orElseThrow(() -> new ResourceNotFoundException("Bidding process not found"))
        );

        entity.setSupplier(
                supplierRepository.findById(dto.supplierId())
                        .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"))
        );

        if (dto.executionOrderId() != null) {
            entity.setExecutionOrder(
                    executionOrderRepository.findById(dto.executionOrderId())
                            .orElseThrow(() -> new ResourceNotFoundException("Execution order not found"))
            );
        }

        if (dto.effortId() != null) {
            entity.setEffort(
                    effortRepository.findById(dto.effortId())
                            .orElseThrow(() -> new ResourceNotFoundException("Effort not found"))
            );
        }

        return mapper.toDTO(repository.save(entity));
    }

    public Page<ContractDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public ContractDTO findById(UUID id) {
        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Contract not found"))
        );
    }

    public ContractDTO update(UUID id, UpdateContractDTO dto) {
        Contract entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        if (dto.number() != null) entity.setNumber(dto.number());
        if (dto.object() != null) entity.setObject(dto.object());
        if (dto.amount() != null) entity.setAmount(dto.amount());
        if (dto.assignedDate() != null) entity.setAssignedDate(dto.assignedDate());
        if (dto.startDate() != null) entity.setStartDate(dto.startDate());
        if (dto.endDate() != null) entity.setEndDate(dto.endDate());
        if (dto.responsibleId() != null) entity.setResponsibleId(dto.responsibleId());

        if (dto.biddingProcessId() != null) {
            entity.setBiddingProcess(
                    biddingRepository.findById(dto.biddingProcessId())
                            .orElseThrow(() -> new ResourceNotFoundException("Bidding process not found"))
            );
        }

        if (dto.supplierId() != null) {
            entity.setSupplier(
                    supplierRepository.findById(dto.supplierId())
                            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"))
            );
        }

        if (dto.executionOrderId() != null) {
            entity.setExecutionOrder(
                    executionOrderRepository.findById(dto.executionOrderId())
                            .orElseThrow(() -> new ResourceNotFoundException("Execution order not found"))
            );
        }

        if (dto.effortId() != null) {
            entity.setEffort(
                    effortRepository.findById(dto.effortId())
                            .orElseThrow(() -> new ResourceNotFoundException("Effort not found"))
            );
        }

        return mapper.toDTO(repository.save(entity));
    }

    public void delete(UUID id) {
        Contract entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        repository.delete(entity);
    }
}