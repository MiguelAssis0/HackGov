package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.ProcessState.*;
import com.fiap.hackgov.bidding.internal.entities.ProcessState;
import com.fiap.hackgov.bidding.internal.mappers.ProcessStateMapper;
import com.fiap.hackgov.bidding.internal.repositories.ProcessStateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessStateService {

    private final ProcessStateRepository repository;
    private final ProcessStateMapper mapper;

    public ProcessStateResponseDTO create(CreateProcessStateDTO dto) {
        ProcessState entity = mapper.toEntity(dto);

        entity.setStartedAt(LocalDateTime.now());

        ProcessState saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    public ProcessStateResponseDTO findById(UUID id) {
        ProcessState entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProcessState not found"));

        return mapper.toDTO(entity);
    }

    public Page<ProcessStateResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDTO);
    }

    public ProcessStateResponseDTO update(UUID id, UpdateProcessStateDTO dto) {
        ProcessState entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProcessState not found"));

        mapper.updateEntity(dto, entity);

        ProcessState updated = repository.save(entity);
        return mapper.toDTO(updated);
    }

    public ProcessStateResponseDTO finish(UUID id) {
        ProcessState entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProcessState not found"));

        entity.setFinishedAt(LocalDateTime.now());

        return mapper.toDTO(repository.save(entity));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("ProcessState not found");
        }
        repository.deleteById(id);
    }
}