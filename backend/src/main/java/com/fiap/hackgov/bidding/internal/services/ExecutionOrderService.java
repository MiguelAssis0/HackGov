package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.ExecutionOrder.*;
import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.entities.enums.OrderStatus;
import com.fiap.hackgov.bidding.internal.mappers.ExecutionOrderMapper;
import com.fiap.hackgov.bidding.internal.repositories.*;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExecutionOrderService {

    private final ExecutionOrderRepository repository;
    private final ExecutionOrderMapper mapper;

    private final ContractRepository contractRepository;
    private final EffortRepository effortRepository;

    public ExecutionOrderDTO create(CreateExecutionOrderDTO dto) {

        ExecutionOrder entity = mapper.toEntity(dto);

        entity.setStatus(OrderStatus.CRIADO);

        entity.setContract(
                contractRepository.findById(dto.contractId())
                        .orElseThrow(() -> new ResourceNotFoundException("Contract not found"))
        );

        if (dto.effortId() != null) {
            entity.setEffort(
                    effortRepository.findById(dto.effortId())
                            .orElseThrow(() -> new ResourceNotFoundException("Effort not found"))
            );
        }

        return mapper.toDTO(repository.save(entity));
    }

    public Page<ExecutionOrderDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public ExecutionOrderDTO findById(UUID id) {
        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Execution order not found"))
        );
    }

    public ExecutionOrderDTO update(UUID id, UpdateExecutionOrderDTO dto) {
        ExecutionOrder entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Execution order not found"));

        if (dto.orderType() != null) entity.setOrderType(dto.orderType());
        if (dto.number() != null) entity.setNumber(dto.number());
        if (dto.description() != null) entity.setDescription(dto.description());
        if (dto.emissionDate() != null) entity.setEmissionDate(dto.emissionDate());
        if (dto.expectedDeliveryDate() != null) entity.setExpectedDeliveryDate(dto.expectedDeliveryDate());
        if (dto.actualDeliveryDate() != null) entity.setActualDeliveryDate(dto.actualDeliveryDate());
        if (dto.status() != null) entity.setStatus(dto.status());
        if (dto.responsibleId() != null) entity.setResponsibleId(dto.responsibleId());

        return mapper.toDTO(repository.save(entity));
    }

    public void delete(UUID id) {
        ExecutionOrder entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Execution order not found"));

        repository.delete(entity);
    }
}