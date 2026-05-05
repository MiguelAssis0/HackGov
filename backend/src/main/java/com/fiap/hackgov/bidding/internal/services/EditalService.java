package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.Edital.*;
import com.fiap.hackgov.bidding.internal.entities.Edital;
import com.fiap.hackgov.bidding.internal.mappers.EditalMapper;
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
public class EditalService {

    private final EditalRepository repository;
    private final EditalMapper mapper;
    private final BiddingProcessRepository biddingRepository;

    public EditalDTO create(CreateEditalDTO dto) {

        if (dto.closingDate().before(dto.openingDate())) {
            throw new IllegalArgumentException("Closing date must be after opening date");
        }

        Edital entity = mapper.toEntity(dto);

        entity.setBiddingProcess(
                biddingRepository.findById(dto.biddingProcessId())
                        .orElseThrow(() -> new ResourceNotFoundException("Bidding process not found"))
        );

        return mapper.toDTO(repository.save(entity));
    }

    public Page<EditalDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public EditalDTO findById(UUID id) {
        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Edital not found"))
        );
    }

    public EditalDTO update(UUID id, UpdateEditalDTO dto) {
        Edital entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Edital not found"));

        if (dto.openingDate() != null) entity.setOpeningDate(dto.openingDate());
        if (dto.closingDate() != null) entity.setClosingDate(dto.closingDate());
        if (dto.impugn() != null) entity.setImpugn(dto.impugn());
        if (dto.impugnReason() != null) entity.setImpugnReason(dto.impugnReason());
        if (dto.documentUrl() != null) entity.setDocumentUrl(dto.documentUrl());

        if (dto.biddingProcessId() != null) {
            entity.setBiddingProcess(
                    biddingRepository.findById(dto.biddingProcessId())
                            .orElseThrow(() -> new ResourceNotFoundException("Bidding process not found"))
            );
        }

        return mapper.toDTO(repository.save(entity));
    }

    public void delete(UUID id) {
        Edital entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Edital not found"));

        repository.delete(entity);
    }
}