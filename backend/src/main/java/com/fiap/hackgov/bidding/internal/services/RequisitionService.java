package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.processHistory.ProcessHistoryDTO;
import com.fiap.hackgov.bidding.internal.DTOs.processStatus.AdvanceRequisitionStageDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.RequisitionResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.ETP;
import com.fiap.hackgov.bidding.internal.entities.ProcessHistory;
import com.fiap.hackgov.bidding.internal.entities.ProcessStatus;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.ETPMapper;
import com.fiap.hackgov.bidding.internal.mappers.ProcessHistoryMapper;
import com.fiap.hackgov.bidding.internal.mappers.RequisitionMapper;
import com.fiap.hackgov.bidding.internal.repositories.ProcessHistoryRepository;
import com.fiap.hackgov.bidding.internal.repositories.ProcessStatusRepository;
import com.fiap.hackgov.bidding.internal.repositories.RequisitionRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.pagination.PageResponseDTO;
import com.fiap.hackgov.shared.infra.pagination.PaginationMapper;
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
public class RequisitionService {

    private final RequisitionRepository requisitionRepository;
    private final ProcessStatusRepository processStatusRepository;
    private final ProcessHistoryRepository processHistoryRepository;
    private final ProcessHistoryMapper processHistoryMapper;
    private final PaginationMapper paginationMapper;

    private final RequisitionMapper requisitionMapper;
    private final ETPMapper etpMapper;

    private final SectorRepository sectorRepository;

    public PageResponseDTO<RequisitionResponseDTO> findAll(Pageable pageable) {

        Page<Requisition> requisitions = requisitionRepository.findAll(pageable);

        Page<RequisitionResponseDTO> dtoPage = requisitions.map(requisitionMapper::toDTO);

        return paginationMapper.toDTO(dtoPage);
    }

    public RequisitionResponseDTO findById(UUID id) {

        Requisition requisition = requisitionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + id));

        return requisitionMapper.toDTO(requisition);
    }

    public RequisitionResponseDTO create(Employee responsible, CreateRequisitionDTO dto) {

        Requisition requisition = requisitionMapper.toEntity(dto);

        requisition.setSector(sectorRepository.findById(dto.sectorId()).orElseThrow(() -> new ResourceNotFoundException("Sector not found: " + dto.sectorId())));

        requisition.setResponsible(responsible);

        requisition.setRegisterNumber(generateRequisitionNumber());

        ETP etp = etpMapper.toEntity(dto.etp());

        etp.setRequisition(requisition);

        requisition.setEtp(etp);

        requisition = requisitionRepository.save(requisition);

        ProcessStatus processStatus = requisitionMapper.toInitialProcessStatus(requisition, responsible.getId());

        requisition.setProcessStatus(processStatus);

        processStatusRepository.save(processStatus);

        ProcessHistory history = requisitionMapper.toInitialHistory(requisition, responsible);

        processHistoryRepository.save(history);

        return requisitionMapper.toDTO(requisition);
    }

    @Transactional
    public RequisitionResponseDTO advanceStage(UUID requisitionId, AdvanceRequisitionStageDTO dto, Employee employee) {

        Requisition requisition = requisitionRepository.findById(requisitionId).orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + requisitionId));

        ProcessStatus processStatus = requisition.getProcessStatus();

        ProcessStage currentStage = processStatus.getStage();
        ProcessStage nextStage = dto.nextStage();

        validateStageTransition(currentStage, nextStage);

        LocalDateTime now = LocalDateTime.now();

        processStatus.setStage(nextStage);
        processStatus.setResponsibleId(employee.getId());
        processStatus.setObservation(dto.observation());

        processStatus.setFinishedAt(now);

        processStatusRepository.save(processStatus);

        ProcessHistory history = new ProcessHistory();

        history.setRequisition(requisition);
        history.setStage(nextStage);
        history.setChangedBy(employee);
        history.setObservation(dto.observation());
        history.setChangedAt(now);

        processHistoryRepository.save(history);

        return requisitionMapper.toDTO(requisition);
    }

    @Transactional(readOnly = true)
    public List<ProcessHistoryDTO> getHistory(UUID requisitionId) {

        Requisition requisition = requisitionRepository.findById(requisitionId).orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + requisitionId));

        List<ProcessHistory> history = processHistoryRepository.findByRequisitionIdOrderByChangedAtAsc(requisition.getId());

        return processHistoryMapper.toDTOList(history);
    }

    public String generateRequisitionNumber() {

        String year = String.valueOf(LocalDate.now().getYear());

        List<Requisition> requisitions = requisitionRepository.findByRegisterNumberStartingWithOrderByRegisterNumberDesc("REQ-" + year + "-", PageRequest.of(0, 1));

        int nextNumber = 1;

        if (!requisitions.isEmpty()) {

            String lastNumber = requisitions.get(0).getRegisterNumber();

            String numericPart = lastNumber.substring(lastNumber.lastIndexOf("-") + 1);

            nextNumber = Integer.parseInt(numericPart) + 1;
        }

        return String.format("REQ-%s-%06d", year, nextNumber);
    }

    private void validateStageTransition(ProcessStage currentStage, ProcessStage nextStage) {

        if (currentStage == nextStage) {

            throw new BusinessException("Requisition is already in this stage");
        }

        if (nextStage.getStep() != currentStage.getStep() + 1) {

            throw new BusinessException("Invalid workflow transition");
        }
    }

}