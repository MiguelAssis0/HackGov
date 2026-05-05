package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.PaymentStatement.*;
import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.entities.enums.PaymentStatus;
import com.fiap.hackgov.bidding.internal.mappers.PaymentStatementMapper;
import com.fiap.hackgov.bidding.internal.repositories.*;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentStatementService {

    private final PaymentStatementRepository repository;
    private final PaymentStatementMapper mapper;
    private final EffortRepository effortRepository;

    public PaymentStatementDTO create(CreatePaymentStatementDTO dto) {

        Effort effort = effortRepository.findById(dto.effortId())
                .orElseThrow(() -> new ResourceNotFoundException("Effort not found"));

        PaymentStatement entity = mapper.toEntity(dto);

        entity.setEffort(effort);
        entity.setStatus(PaymentStatus.PENDENTE);
        entity.setApproved(false);

        return mapper.toDTO(repository.save(entity));
    }

    public Page<PaymentStatementDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public PaymentStatementDTO findById(UUID id) {
        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment statement not found"))
        );
    }

    public PaymentStatementDTO update(UUID id, UpdatePaymentStatementDTO dto) {
        PaymentStatement entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment statement not found"));

        if (dto.declarationType() != null) entity.setDeclarationType(dto.declarationType());
        if (dto.responsibleId() != null) entity.setResponsibleId(dto.responsibleId());
        if (dto.amount() != null) entity.setAmount(dto.amount());
        if (dto.dueDate() != null) entity.setDueDate(dto.dueDate());
        if (dto.paymentDate() != null) entity.setPaymentDate(dto.paymentDate());
        if (dto.status() != null) entity.setStatus(dto.status());
        if (dto.approved() != null) entity.setApproved(dto.approved());

        if (dto.effortId() != null) {
            entity.setEffort(
                    effortRepository.findById(dto.effortId())
                            .orElseThrow(() -> new ResourceNotFoundException("Effort not found"))
            );
        }

        return mapper.toDTO(repository.save(entity));
    }

    public void delete(UUID id) {
        PaymentStatement entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment statement not found"));

        repository.delete(entity);
    }
}