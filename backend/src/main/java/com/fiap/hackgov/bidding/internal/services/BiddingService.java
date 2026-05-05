package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.Bidding.*;
import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.entities.enums.BiddingStatus;
import com.fiap.hackgov.bidding.internal.mappers.BiddingMapper;
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
public class BiddingService {

    private final BiddingProcessRepository repository;
    private final BiddingMapper mapper;

    private final RequisitionRepository requisitionRepository;
    private final EditalRepository editalRepository;
    private final SupplierRepository supplierRepository;

    public BiddingProcessDTO create(CreateBiddingProcessDTO dto) {

        if (dto.openingDate().before(dto.legalDeadline())) {
            throw new IllegalArgumentException("Opening date must be after legal deadline");
        }

        BiddingProcess entity = mapper.toEntity(dto);

        entity.setRequisition(
                requisitionRepository.findById(dto.requisitionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Requisition not found"))
        );

        if (dto.editalId() != null) {
            entity.setEdital(
                    editalRepository.findById(dto.editalId())
                            .orElseThrow(() -> new ResourceNotFoundException("Edital not found"))
            );
        }

        if (dto.winningSupplierId() != null) {
            entity.setWinningSupplier(
                    supplierRepository.findById(dto.winningSupplierId())
                            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"))
            );
        }

        return mapper.toDTO(repository.save(entity));
    }

    public Page<BiddingProcessDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public BiddingProcessDTO findById(UUID id) {
        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Bidding process not found"))
        );
    }

    public BiddingProcessDTO update(UUID id, UpdateBiddingProcessDTO dto) {
        BiddingProcess entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bidding process not found"));

        // Regra: não pode alterar processo finalizado
        if (entity.getStatus() == BiddingStatus.FINALIZDO) {
            throw new IllegalStateException("Cannot update a finished bidding process");
        }

        if (dto.type() != null) entity.setType(dto.type());
        if (dto.legalDeadline() != null) entity.setLegalDeadline(dto.legalDeadline());
        if (dto.openingDate() != null) entity.setOpeningDate(dto.openingDate());
        if (dto.status() != null) entity.setStatus(dto.status());
        if (dto.responsibleId() != null) entity.setResponsibleId(dto.responsibleId());

        if (dto.editalId() != null) {
            entity.setEdital(
                    editalRepository.findById(dto.editalId())
                            .orElseThrow(() -> new ResourceNotFoundException("Edital not found"))
            );
        }

        if (dto.winningSupplierId() != null) {
            entity.setWinningSupplier(
                    supplierRepository.findById(dto.winningSupplierId())
                            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"))
            );
        }

        return mapper.toDTO(repository.save(entity));
    }

    public void delete(UUID id) {
        BiddingProcess entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bidding process not found"));

        if (entity.getStatus() == BiddingStatus.ANDAMENTO) {
            throw new IllegalStateException("Cannot delete a process in progress");
        }

        repository.delete(entity);
    }
}