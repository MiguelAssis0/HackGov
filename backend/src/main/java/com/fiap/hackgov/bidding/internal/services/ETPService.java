package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.ETP.*;
import com.fiap.hackgov.bidding.internal.entities.ETP;
import com.fiap.hackgov.bidding.internal.mappers.ETPMapper;
import com.fiap.hackgov.bidding.internal.repositories.*;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ETPService {

    private final ETPRepository repository;
    private final ETPMapper mapper;
    private final RequisitionRepository requisitionRepository;

    public ETPDTO create(CreateETPDTO dto) {

        if (repository.existsByRequisitionId(dto.requisitionId())) {
            throw new IllegalStateException("ETP already exists for this requisition");
        }

        ETP entity = mapper.toEntity(dto);

        entity.setRequisition(
                requisitionRepository.findById(dto.requisitionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Requisition not found"))
        );

        return mapper.toDTO(repository.save(entity));
    }

    public Page<ETPDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public ETPDTO findById(UUID id) {
        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("ETP not found"))
        );
    }

    public ETPDTO update(UUID id, UpdateETPDTO dto) {
        ETP entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ETP not found"));

        if (dto.content() != null) {
            entity.setContent(dto.content());
        }

        return mapper.toDTO(repository.save(entity));
    }

    public void delete(UUID id) {
        ETP entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ETP not found"));

        repository.delete(entity);
    }
}