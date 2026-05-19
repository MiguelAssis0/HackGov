package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.licitation.CreateLicitationProcessDTO;
import com.fiap.hackgov.bidding.internal.entities.LicitationHistory;
import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.LicitationProcessMapper;
import com.fiap.hackgov.bidding.internal.repositories.LicitationHistoryRepository;
import com.fiap.hackgov.bidding.internal.repositories.LicitationProcessRepository;
import com.fiap.hackgov.bidding.internal.repositories.RequisitionRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class LicitationProcessService {

    private final LicitationProcessRepository licitationProcessRepository;
    private final LicitationHistoryRepository licitationHistoryRepository;
    private final RequisitionRepository requisitionRepository;
    private final LicitationProcessMapper licitationProcessMapper;

    public LicitationProcess create(CreateLicitationProcessDTO dto, Employee employee) {

        Requisition requisition = requisitionRepository.findById(dto.requisitionId()).orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + dto.requisitionId()));

        validateRequisitionEligibility(requisition);

        validateDateRange(dto);

        licitationProcessRepository.findByRequisitionId(requisition.getId()).ifPresent(lp -> {
            throw new BusinessException("Requisition already linked to a licitation process");
        });

        LicitationProcess licitationProcess = licitationProcessMapper.toEntity(dto);

        licitationProcess.setProcessNumber(generateProcessNumber());

        licitationProcess.setRequisition(requisition);

        licitationProcess.setStatus(LicitationStatus.DRAFT);

        licitationProcess = licitationProcessRepository.save(licitationProcess);

        createHistory(licitationProcess, employee, LicitationEventType.PROCESS_CREATED, LicitationStatus.DRAFT, "Processo licitatório criado");

        return licitationProcess;
    }

    @Transactional(readOnly = true)
    public Page<LicitationProcess> findAll(Pageable pageable) {

        return licitationProcessRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public LicitationProcess findById(UUID id) {

        return licitationProcessRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Licitation process not found: " + id));
    }

    public LicitationProcess updateStatus(UUID id, LicitationStatus status, String observation, Employee employee) {

        LicitationProcess licitationProcess = findById(id);

        licitationProcess.setStatus(status);

        licitationProcess = licitationProcessRepository.save(licitationProcess);

        createHistory(licitationProcess, employee, LicitationEventType.STATUS_CHANGED, status, observation);

        return licitationProcess;
    }

    @Transactional(readOnly = true)
    public List<LicitationHistory> getHistory(UUID processId) {

        return licitationHistoryRepository.findByLicitationProcessIdOrderByChangedAtAsc(processId);
    }

    private void validateRequisitionEligibility(Requisition requisition) {

        ProcessStage currentStage = requisition.getProcessStatus().getStage();

        if (currentStage != ProcessStage.PROCESSO_LICITATORIO) {
            throw new BusinessException("Requisition is not ready for licitation process creation");
        }
    }

    private String generateProcessNumber() {

        String year = String.valueOf(LocalDate.now().getYear());

        List<LicitationProcess> processes = licitationProcessRepository.findByProcessNumberStartingWithOrderByProcessNumberDesc("LIC-" + year + "-", PageRequest.of(0, 1));

        int nextNumber = 1;

        if (!processes.isEmpty()) {

            String lastNumber = processes.getFirst().getProcessNumber();

            String numericPart = lastNumber.substring(lastNumber.lastIndexOf("-") + 1);

            nextNumber = Integer.parseInt(numericPart) + 1;
        }

        return String.format("LIC-%s-%06d", year, nextNumber);
    }

    void createHistory(LicitationProcess licitationProcess, Employee employee, LicitationEventType eventType, LicitationStatus status, String observation) {

        LicitationHistory history = new LicitationHistory();

        history.setLicitationProcess(licitationProcess);

        history.setEventType(eventType);

        history.setStatus(status);

        history.setChangedBy(employee);

        history.setObservation(observation);

        history.setChangedAt(LocalDateTime.now());

        licitationHistoryRepository.save(history);
    }

    private void validateDateRange(CreateLicitationProcessDTO dto) {

        if (dto.closingDate().isBefore(dto.openingDate())) {

            throw new BusinessException("Closing date cannot be before opening date");
        }
    }

}
