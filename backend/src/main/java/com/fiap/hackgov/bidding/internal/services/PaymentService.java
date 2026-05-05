package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.Payment.*;
import com.fiap.hackgov.bidding.internal.entities.Payment;
import com.fiap.hackgov.bidding.internal.mappers.PaymentMapper;
import com.fiap.hackgov.bidding.internal.repositories.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;

    public PaymentDTO create(CreatePaymentDTO dto) {
        Payment entity = mapper.toEntity(dto);
        Payment saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    public PaymentDTO findById(UUID id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        return mapper.toDTO(payment);
    }

    public Page<PaymentDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDTO);
    }

    public PaymentDTO update(UUID id, UpdatePaymentDTO dto) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        mapper.updateEntity(dto, payment);

        Payment updated = repository.save(payment);
        return mapper.toDTO(updated);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Payment not found");
        }
        repository.deleteById(id);
    }
}