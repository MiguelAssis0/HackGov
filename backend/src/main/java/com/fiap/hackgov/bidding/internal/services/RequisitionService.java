package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.processHistory.ProcessHistoryDTO;
import com.fiap.hackgov.bidding.internal.DTOs.processStatus.AdvanceRequisitionStageDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.AssignProcurementResponsibleDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.RequisitionResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.RequisitionResponsibleDTO;
import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.entities.enums.*;
import com.fiap.hackgov.bidding.internal.mappers.ETPMapper;
import com.fiap.hackgov.bidding.internal.mappers.ProcessHistoryMapper;
import com.fiap.hackgov.bidding.internal.mappers.RequisitionMapper;
import com.fiap.hackgov.bidding.internal.repositories.*;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.inbox.internal.services.InboxService;
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
import java.util.Locale;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class RequisitionService {

    private final RequisitionRepository requisitionRepository;
    private final ProcessStatusRepository processStatusRepository;
    private final ProcessHistoryRepository processHistoryRepository;
    private final SectorRepository sectorRepository;
    private final EmployeeRepository employeeRepository;
    private final ApprovalRepository approvalRepository;
    private final AnalysisRepository analysisRepository;
    private final ProcessHistoryService processHistoryService;
    private final InboxService inboxService;

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

        if (responsible == null) {
            throw new BusinessException("Usuário autenticado não identificado");
        }

        Requisition requisition = requisitionMapper.toEntity(dto);

        Sector sector = sectorRepository.findById(dto.sectorId())
                .orElseThrow(() -> new BusinessException("O setor selecionado não existe mais. Atualize a página e selecione novamente."));

        if (responsible.getCityHallId() == null
                || sector.getCityHall() == null
                || !responsible.getCityHallId().getId().equals(sector.getCityHall().getId())) {
            throw new BusinessException("O setor selecionado não pertence à prefeitura do usuário");
        }

        requisition.setSector(sector);

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

        sendToNextStage(requisition, ProcessStage.HOMOLOGACAO_SECRETARIO, responsible, "Requisição enviada para homologação do secretário");

        return requisitionMapper.toDTO(requisition);
    }

    @Transactional
    public RequisitionResponseDTO advanceStage(UUID requisitionId, AdvanceRequisitionStageDTO dto, Employee employee) {

        Requisition requisition = requisitionRepository.findById(requisitionId).orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + requisitionId));

        Employee stageResponsible = validateContractStageResponsible(requisition, dto.nextStage(), employee);

        sendToNextStage(requisition, dto.nextStage(), stageResponsible, dto.observation());

        return requisitionMapper.toDTO(requisition);
    }

    @Transactional(readOnly = true)
    public List<RequisitionResponsibleDTO> findProcurementEmployees(UUID requisitionId) {

        Requisition requisition = findEntityById(requisitionId);

        UUID cityHallId = requisition.getSector().getCityHall().getId();

        return employeeRepository.findActiveProcurementEmployees(cityHallId)
                .stream()
                .map(requisitionMapper::mapResponsible)
                .toList();
    }

    public RequisitionResponseDTO assignProcurementResponsible(
            UUID requisitionId,
            AssignProcurementResponsibleDTO dto,
            Employee assignedBy
    ) {

        Requisition requisition = findEntityById(requisitionId);

        if (requisition.getProcessStatus().getStage() != ProcessStage.RECEBIMENTO_COMPRAS) {
            throw new BusinessException("Procurement responsible can only be assigned during procurement receipt");
        }

        Employee procurementResponsible = employeeRepository.findByIdWithDetails(dto.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.employeeId()));

        validateProcurementResponsible(requisition, procurementResponsible);

        requisition.setProcurementResponsible(procurementResponsible);
        requisitionRepository.save(requisition);

        String observation = "Responsável de compras definido: " + procurementResponsible.getFullName();

        processHistoryService.createProcessHistory(
                requisition,
                assignedBy,
                observation,
                ProcessStage.RECEBIMENTO_COMPRAS,
                HistoryEventType.COMMENT_ADDED
        );

        sendToNextStage(requisition, ProcessStage.ANALISE_REQUISICAO, assignedBy, observation);

        requisition.getProcessStatus().setResponsibleId(procurementResponsible.getId());
        processStatusRepository.save(requisition.getProcessStatus());

        return requisitionMapper.toDTO(requisition);
    }

    void sendToNextStage(Requisition requisition, ProcessStage nextStage, Employee employee, String observation) {

        ProcessStatus processStatus = requisition.getProcessStatus();

        ProcessStage currentStage = processStatus.getStage();

        validateStageTransition(requisition, currentStage, nextStage);

        updateCurrentStage(processStatus, nextStage, employee, observation);

        processHistoryService.createProcessHistory(requisition, employee, observation, nextStage, HistoryEventType.STAGE_SENT);

        createApprovalIfNecessary(requisition, nextStage);

        createAnalysisIfNecessary(requisition, nextStage);

        inboxService.notifyBiddingStage(requisition, nextStage, employee);
    }

    void returnToInitialStage(Requisition requisition, Employee employee, String observation) {

        ProcessStatus processStatus = requisition.getProcessStatus();

        updateCurrentStage(processStatus, ProcessStage.REQUISICAO_CADASTRADA, employee, observation);

        processHistoryService.createProcessHistory(requisition, employee, observation, ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.STAGE_SENT);

        createAnalysisIfNecessary(requisition, ProcessStage.REQUISICAO_CADASTRADA);
    }

    void finishWorkflow(Requisition requisition, Employee employee, boolean approved) {
        ProcessStatus processStatus = requisition.getProcessStatus();
        LocalDateTime finishedAt = LocalDateTime.now();

        processStatus.setResponsibleId(employee.getId());
        processStatus.setObservation(
                approved
                        ? "Processo concluído com prestação de contas homologada"
                        : "Processo encerrado com prestação de contas reprovada"
        );
        processStatus.setFinishedAt(finishedAt);
        processStatusRepository.save(processStatus);

        requisition.setRequestStatus(approved ? RequestStatus.APROVADA : RequestStatus.REPROVADA);
        requisitionRepository.save(requisition);
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

    private Requisition findEntityById(UUID id) {
        return requisitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + id));
    }

    private void validateProcurementResponsible(Requisition requisition, Employee employee) {

        if (employee.getCityHallId() == null
                || !employee.getCityHallId().getId().equals(requisition.getSector().getCityHall().getId())) {
            throw new BusinessException("Procurement responsible must belong to the requisition city hall");
        }

        if (employee.getSectorId() == null
                || !employee.getSectorId().getName().toLowerCase().contains("compras")) {
            throw new BusinessException("Procurement responsible must belong to the procurement sector");
        }

        if (!employee.getStatus()) {
            throw new BusinessException("Procurement responsible must be active");
        }
    }

    private Employee validateContractStageResponsible(
            Requisition requisition,
            ProcessStage nextStage,
            Employee authenticatedEmployee
    ) {
        if (requisition.getProcessStatus().getStage() != ProcessStage.SETOR_CONTRATOS
                || nextStage != ProcessStage.INICIO_SERVICOS) {
            return authenticatedEmployee;
        }

        if (authenticatedEmployee == null) {
            throw new BusinessException("Usuário autenticado não identificado");
        }

        Employee employee = employeeRepository.findByIdWithDetails(authenticatedEmployee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + authenticatedEmployee.getId()));

        if (!Boolean.TRUE.equals(employee.getStatus())) {
            throw new BusinessException("Somente um servidor ativo do setor de Contratos pode aprovar esta etapa");
        }

        if (employee.getSectorId() == null
                || employee.getSectorId().getName() == null
                || !employee.getSectorId().getName().toLowerCase(Locale.ROOT).contains("contrat")) {
            throw new BusinessException("Somente um servidor do setor de Contratos pode aprovar esta etapa");
        }

        UUID requisitionCityHallId = requisition.getSector().getCityHall().getId();
        if (employee.getCityHallId() == null
                || !employee.getCityHallId().getId().equals(requisitionCityHallId)) {
            throw new BusinessException("O servidor do setor de Contratos deve pertencer à prefeitura da requisição");
        }

        return employee;
    }

    private void validateStageTransition(Requisition requisition, ProcessStage currentStage, ProcessStage nextStage) {

        if (nextStage.getStep() < currentStage.getStep()) {
            throw new BusinessException("Cannot rollback workflow stage");
        }

        if (nextStage.getStep() > currentStage.getStep() + 1) {
            throw new BusinessException("Invalid workflow transition");
        }

        ApprovalSector requiredApproval = mapApprovalSector(currentStage);

        if (requiredApproval != null) {
            Approval latestApproval = approvalRepository.findFirstByRequisitionIdAndApprovalSectorOrderByCreatedAtDesc(requisition.getId(), requiredApproval)
                    .orElseThrow(() -> new BusinessException("Approval not found for current stage"));

            if (latestApproval.getApprovalStatus() != ApprovalStatus.APROVADO) {

                throw new BusinessException("Current stage approval is still pending");
            }
        }

        if (isNotAnalysisStage(currentStage)) {
            return;
        }

        Analysis latestAnalysis = analysisRepository.findFirstByRequisitionIdAndStageOrderByCreatedAtDesc(requisition.getId(), currentStage)
                .orElseThrow(() -> new BusinessException("Analysis not found for current stage"));

        if (latestAnalysis.getResult() != AnalysisResult.APROVADO) {
            throw new BusinessException("Current stage analysis is still pending");
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

    private void createAnalysisIfNecessary(Requisition requisition, ProcessStage stage) {

        if (isNotAnalysisStage(stage)) {
            return;
        }

        Analysis analysis = new Analysis();

        analysis.setRequisition(requisition);
        analysis.setStage(stage);
        analysis.setResult(AnalysisResult.PENDENTE);

        analysisRepository.save(analysis);
    }

    private void updateCurrentStage(ProcessStatus processStatus, ProcessStage stage, Employee employee, String observation) {

        processStatus.setStage(stage);
        processStatus.setResponsibleId(employee.getId());
        processStatus.setObservation(observation);
        processStatus.setStartedAt(LocalDateTime.now());
        processStatus.setFinishedAt(null);

        processStatusRepository.save(processStatus);
    }

    private ApprovalSector mapApprovalSector(ProcessStage stage) {

        return switch (stage) {

            case HOMOLOGACAO_SECRETARIO -> ApprovalSector.REQUISICAO_SECRETARIO;

            case HOMOLOGACAO_COMPRAS -> ApprovalSector.ANALISE_COMPRAS;

            case DECLARACAO_PAGAMENTO -> ApprovalSector.DECLARACAO_PAGAMENTO;

            case HOMOLOGACAO_PRESTACAO_CONTAS -> ApprovalSector.PRESTACAO_CONTAS;

            default -> null;
        };
    }

    private boolean isNotAnalysisStage(ProcessStage stage) {

        return stage != ProcessStage.ANALISE_REQUISICAO && stage != ProcessStage.ANALISE_PRESTACAO_CONTAS;
    }
}
