package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.licitation.CreateLicitationProcessDTO;
import com.fiap.hackgov.bidding.internal.DTOs.licitation.PublishLicitationResultDTO;
import com.fiap.hackgov.bidding.internal.entities.LicitationHistory;
import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.Supplier;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.LicitationProcessMapper;
import com.fiap.hackgov.bidding.internal.mappers.SupplierMapper;
import com.fiap.hackgov.bidding.internal.repositories.LicitationHistoryRepository;
import com.fiap.hackgov.bidding.internal.repositories.LicitationProcessRepository;
import com.fiap.hackgov.bidding.internal.repositories.RequisitionRepository;
import com.fiap.hackgov.bidding.internal.repositories.SupplierRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
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
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class LicitationProcessService {

    private final LicitationProcessRepository licitationProcessRepository;
    private final LicitationHistoryRepository licitationHistoryRepository;
    private final RequisitionRepository requisitionRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final LicitationProcessMapper licitationProcessMapper;
    private final SupplierMapper supplierMapper;
    private final RequisitionService requisitionService;

    private static final Set<LicitationStatus> RESULT_STATUSES = Set.of(
            LicitationStatus.IN_PROGRESS,
            LicitationStatus.FINISHED,
            LicitationStatus.IMPUGNED,
            LicitationStatus.POSTPONED,
            LicitationStatus.CLOSED
    );

    public LicitationProcess create(CreateLicitationProcessDTO dto, Employee employee) {

        Requisition requisition = requisitionRepository.findById(dto.requisitionId()).orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + dto.requisitionId()));

        validateRequisitionEligibility(requisition);

        validateDateRange(dto);

        Employee responsible = employeeRepository.findByIdWithDetails(dto.responsibleId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.responsibleId()));

        validateResponsible(requisition, responsible);

        licitationProcessRepository.findByRequisitionId(requisition.getId()).ifPresent(lp -> {
            throw new BusinessException("Requisition already linked to a licitation process");
        });

        LicitationProcess licitationProcess = licitationProcessMapper.toEntity(dto);

        licitationProcess.setProcessNumber(generateProcessNumber());

        licitationProcess.setRequisition(requisition);

        licitationProcess.setResponsible(responsible);

        licitationProcess.setStatus(LicitationStatus.DRAFT);

        licitationProcess = licitationProcessRepository.save(licitationProcess);

        createHistory(licitationProcess, employee, LicitationEventType.PROCESS_CREATED, LicitationStatus.DRAFT, "Processo licitatório criado");

        requisitionService.sendToNextStage(
                requisition,
                ProcessStage.PROCESSO_LICITATORIO,
                employee,
                "Processo licitatório criado e atribuído a " + responsible.getFullName()
        );

        requisition.getProcessStatus().setResponsibleId(responsible.getId());

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

    @Transactional(readOnly = true)
    public LicitationProcess findByRequisitionId(UUID requisitionId) {

        return licitationProcessRepository.findByRequisitionId(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Licitation process not found for requisition: " + requisitionId));
    }

    public LicitationProcess updateStatus(UUID id, LicitationStatus status, String observation, Employee employee) {

        LicitationProcess licitationProcess = findById(id);

        if (RESULT_STATUSES.contains(status)) {
            throw new BusinessException("Licitation result statuses must be published through the result endpoint");
        }

        licitationProcess.setStatus(status);

        licitationProcess = licitationProcessRepository.save(licitationProcess);

        createHistory(licitationProcess, employee, LicitationEventType.STATUS_CHANGED, status, observation);

        return licitationProcess;
    }

    public LicitationProcess publishResult(UUID id, PublishLicitationResultDTO dto, Employee employee) {

        LicitationProcess licitationProcess = findById(id);

        validateResultResponsible(licitationProcess, employee);

        if (licitationProcess.getRequisition().getProcessStatus().getStage() != ProcessStage.PROCESSO_LICITATORIO) {
            throw new BusinessException("Licitation result can only be changed during the licitation process stage");
        }

        if (!RESULT_STATUSES.contains(dto.status())) {
            throw new BusinessException("Invalid status for licitation result publication");
        }

        if (dto.status() == LicitationStatus.FINISHED) {
            if (dto.winnerSupplier() == null) {
                throw new BusinessException("Winning supplier is required for a finished licitation process");
            }

            Supplier winnerSupplier = supplierRepository.findByCnpj(dto.winnerSupplier().cnpj())
                    .map(existing -> {
                        supplierMapper.updateEntity(dto.winnerSupplier(), existing);
                        existing.setActive(true);
                        return existing;
                    })
                    .orElseGet(() -> {
                        Supplier supplier = supplierMapper.toEntity(dto.winnerSupplier());
                        supplier.setActive(true);
                        return supplier;
                    });

            licitationProcess.setWinnerSupplier(supplierRepository.save(winnerSupplier));
        } else {
            if (dto.winnerSupplier() != null) {
                throw new BusinessException("Winning supplier can only be registered when the status is finished");
            }
        }

        licitationProcess.setStatus(dto.status());
        licitationProcess = licitationProcessRepository.save(licitationProcess);

        createHistory(
                licitationProcess,
                employee,
                dto.status() == LicitationStatus.FINISHED
                        ? LicitationEventType.PROCESS_FINISHED
                        : LicitationEventType.STATUS_CHANGED,
                dto.status(),
                dto.observation()
        );

        if (dto.status() == LicitationStatus.FINISHED) {
            requisitionService.sendToNextStage(
                    licitationProcess.getRequisition(),
                    ProcessStage.SETOR_CONTRATOS,
                    employee,
                    "Licitação finalizada com empresa vencedora: "
                            + licitationProcess.getWinnerSupplier().getCorporateName()
            );
        }

        return licitationProcess;
    }

    @Transactional(readOnly = true)
    public List<LicitationHistory> getHistory(UUID processId) {

        return licitationHistoryRepository.findByLicitationProcessIdOrderByChangedAtAsc(processId);
    }

    private void validateRequisitionEligibility(Requisition requisition) {

        ProcessStage currentStage = requisition.getProcessStatus().getStage();

        if (currentStage != ProcessStage.COMPOSICAO_PROCESSO) {
            throw new BusinessException("Requisition is not ready for licitation process creation");
        }
    }

    private void validateResponsible(Requisition requisition, Employee responsible) {

        if (responsible.getCityHallId() == null
                || !responsible.getCityHallId().getId().equals(requisition.getSector().getCityHall().getId())) {
            throw new BusinessException("Licitation responsible must belong to the requisition city hall");
        }

        if (responsible.getSectorId() == null
                || !responsible.getSectorId().getName().toLowerCase().contains("compras")) {
            throw new BusinessException("Licitation responsible must belong to the procurement sector");
        }

        if (!responsible.getStatus()) {
            throw new BusinessException("Licitation responsible must be active");
        }
    }

    private void validateResultResponsible(LicitationProcess licitationProcess, Employee employee) {

        if (employee == null
                || licitationProcess.getResponsible() == null
                || !licitationProcess.getResponsible().getId().equals(employee.getId())) {
            throw new BusinessException("Only the assigned licitation responsible can publish the result");
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
