package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.processHistory.ProcessHistoryDTO;
import com.fiap.hackgov.bidding.internal.DTOs.processStatus.AdvanceRequisitionStageDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.RequisitionResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalSector;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.HistoryEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.ETPMapper;
import com.fiap.hackgov.bidding.internal.mappers.ProcessHistoryMapper;
import com.fiap.hackgov.bidding.internal.mappers.RequisitionMapper;
import com.fiap.hackgov.bidding.internal.repositories.ApprovalRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class RequisitionService {

    private final RequisitionRepository requisitionRepository;
    private final ProcessStatusRepository processStatusRepository;
    private final ProcessHistoryRepository processHistoryRepository;
    private final SectorRepository sectorRepository;
    private final ApprovalRepository approvalRepository;
    private final ProcessHistoryService processHistoryService;

    private final ProcessHistoryMapper processHistoryMapper;
    private final PaginationMapper paginationMapper;
    private final RequisitionMapper requisitionMapper;
    private final ETPMapper etpMapper;

    public PageResponseDTO<RequisitionResponseDTO> findAll(Pageable pageable) {

        Page<Requisition> requisitions = requisitionRepository.findAll(pageable);

        Page<RequisitionResponseDTO> dtoPage = requisitions.map(requisitionMapper::toDTO);

        return paginationMapper.toDTO(dtoPage);
    }

    public RequisitionResponseDTO findById(UUID id) {

        Requisition requisition = requisitionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + id));

        return requisitionMapper.toDTO(requisition);
    }

    @Transactional
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

        processHistoryService.createProcessHistory(requisition, responsible, "Requisição criada", ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.REQUISITION_CREATED);

        processHistoryService.createProcessHistory(requisition, responsible, "Requisição enviada para homologação do secretário", ProcessStage.HOMOLOGACAO_SECRETARIO, HistoryEventType.STAGE_SENT);

        createApprovalIfNecessary(requisition, ProcessStage.REQUISICAO_CADASTRADA);

        return requisitionMapper.toDTO(requisition);
    }

    @Transactional
    public RequisitionResponseDTO advanceStage(UUID requisitionId, AdvanceRequisitionStageDTO dto, Employee employee) {

        Requisition requisition = requisitionRepository.findById(requisitionId).orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + requisitionId));

        sendToNextStage(requisition, dto.nextStage(), employee, dto.observation());

        return requisitionMapper.toDTO(requisition);
    }

    void sendToNextStage(Requisition requisition, ProcessStage nextStage, Employee employee, String observation) {

        ProcessStatus processStatus = requisition.getProcessStatus();

        ProcessStage currentStage = processStatus.getStage();

        validateStageTransition(requisition, currentStage, nextStage);

        updateCurrentStage(processStatus, nextStage, employee, observation);

        processHistoryService.createProcessHistory(requisition, employee, observation, nextStage, HistoryEventType.STAGE_SENT);

        createApprovalIfNecessary(requisition, nextStage);
    }

    void returnToInitialStage(Requisition requisition, Employee employee, String observation) {

        ProcessStatus processStatus = requisition.getProcessStatus();

        updateCurrentStage(processStatus, ProcessStage.REQUISICAO_CADASTRADA, employee, observation);

        processHistoryService.createProcessHistory(requisition, employee, observation, ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.STAGE_SENT);

        createApprovalIfNecessary(requisition, ProcessStage.REQUISICAO_CADASTRADA);
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

            String lastNumber = requisitions.getFirst().getRegisterNumber();

            String numericPart = lastNumber.substring(lastNumber.lastIndexOf("-") + 1);

            nextNumber = Integer.parseInt(numericPart) + 1;
        }

        return String.format("REQ-%s-%06d", year, nextNumber);
    }

    private void validateStageTransition(Requisition requisition, ProcessStage currentStage, ProcessStage nextStage) {

        if (nextStage.getStep() < currentStage.getStep()) {
            throw new BusinessException("Cannot rollback workflow stage");
        }

        if (nextStage.getStep() > currentStage.getStep() + 1) {
            throw new BusinessException("Invalid workflow transition");
        }

        ApprovalSector requiredApproval = mapApprovalSector(currentStage);

        if (requiredApproval == null) {
            return;
        }

        Approval latestApproval = requisition.getApprovals().stream().filter(a -> a.getApprovalSector() == requiredApproval).max(Comparator.comparing(Approval::getCreatedAt)).orElseThrow(() -> new BusinessException("Approval not found for current stage"));

        if (latestApproval.getApprovalStatus() != ApprovalStatus.APROVADO) {

            throw new BusinessException("Current stage approval is still pending");
        }
    }

    private void createApprovalIfNecessary(Requisition requisition, ProcessStage stage) {

        ApprovalSector sector = mapApprovalSector(stage);

        if (sector == null) {
            return;
        }

        Approval approval = new Approval();

        approval.setRequisition(requisition);

        approval.setApprovalSector(sector);

        approval.setApprovalStatus(ApprovalStatus.PENDENTE);

        approvalRepository.save(approval);
    }

    void updateCurrentStage(ProcessStatus processStatus, ProcessStage stage, Employee employee, String observation) {

        processStatus.setStage(stage);
        processStatus.setResponsibleId(employee.getId());
        processStatus.setObservation(observation);
        processStatus.setStartedAt(LocalDateTime.now());
        processStatus.setFinishedAt(null);

        processStatusRepository.save(processStatus);
    }

    private ApprovalSector mapApprovalSector(ProcessStage stage) {

        return switch (stage) {

            case REQUISICAO_CADASTRADA -> ApprovalSector.REQUISICAO_SECRETARIO;

            case ANALISE_PRESTACAO_CONTAS -> ApprovalSector.ANALISE_COMPRAS;

            case DECLARACAO_PAGAMENTO -> ApprovalSector.DECLARACAO_PAGAMENTO;

            case PRESTACAO_CONTAS -> ApprovalSector.PRESTACAO_CONTAS;

            default -> null;
        };
    }
}
