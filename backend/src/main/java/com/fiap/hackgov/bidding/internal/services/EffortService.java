package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.Effort.*;
import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.mappers.EffortMapper;
import com.fiap.hackgov.bidding.internal.repositories.*;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EffortService {

    private final EffortRepository repository;
    private final EffortMapper mapper;

    private final ContractRepository contractRepository;
    private final ExecutionOrderRepository executionOrderRepository;
    private final PaymentStatementRepository paymentStatementRepository;

    public EffortDTO create(CreateEffortDTO dto) {

        Contract contract = contractRepository.findById(dto.contractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        // 🔥 Regra crítica: empenho não pode exceder contrato
        if (dto.reservedValue().compareTo(contract.getAmount()) > 0) {
            throw new IllegalArgumentException("Reserved value exceeds contract amount");
        }

        Effort entity = mapper.toEntity(dto);
        entity.setContract(contract);

        if (dto.executionOrderId() != null) {
            entity.setExecutionOrder(
                    executionOrderRepository.findById(dto.executionOrderId())
                            .orElseThrow(() -> new ResourceNotFoundException("Execution order not found"))
            );
        }

        if (dto.paymentStatementId() != null) {
            entity.setPaymentStatement(
                    paymentStatementRepository.findById(dto.paymentStatementId())
                            .orElseThrow(() -> new ResourceNotFoundException("Payment statement not found"))
            );
        }

        return mapper.toDTO(repository.save(entity));
    }

    public Page<EffortDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public EffortDTO findById(UUID id) {
        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Effort not found"))
        );
    }

    public EffortDTO update(UUID id, UpdateEffortDTO dto) {
        Effort entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Effort not found"));

        if (dto.kindCommitment() != null) entity.setKindCommitment(dto.kindCommitment());
        if (dto.reservedValue() != null) entity.setReservedValue(dto.reservedValue());
        if (dto.emitterId() != null) entity.setEmitterId(dto.emitterId());

        if (dto.contractId() != null) {
            Contract contract = contractRepository.findById(dto.contractId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

            // 🔥 mesma validação no update
            if (dto.reservedValue() != null &&
                    dto.reservedValue().compareTo(contract.getAmount()) > 0) {
                throw new IllegalArgumentException("Reserved value exceeds contract amount");
            }

            entity.setContract(contract);
        }

        if (dto.executionOrderId() != null) {
            entity.setExecutionOrder(
                    executionOrderRepository.findById(dto.executionOrderId())
                            .orElseThrow(() -> new ResourceNotFoundException("Execution order not found"))
            );
        }

        if (dto.paymentStatementId() != null) {
            entity.setPaymentStatement(
                    paymentStatementRepository.findById(dto.paymentStatementId())
                            .orElseThrow(() -> new ResourceNotFoundException("Payment statement not found"))
            );
        }

        return mapper.toDTO(repository.save(entity));
    }

    public void delete(UUID id) {
        Effort entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Effort not found"));

        repository.delete(entity);
    }
}