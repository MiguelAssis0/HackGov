package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.approval.ApprovalResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.approval.CreateApprovalDTO;
import com.fiap.hackgov.bidding.internal.DTOs.approval.UpdateApprovalDTO;
import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.entities.enums.*;
import com.fiap.hackgov.bidding.internal.mappers.ApprovalMapper;
import com.fiap.hackgov.bidding.internal.repositories.AccountabilityReportRepository;
import com.fiap.hackgov.bidding.internal.repositories.ApprovalRepository;
import com.fiap.hackgov.bidding.internal.repositories.PaymentDeclarationRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.Actions;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ApprovalService {

    private final RequisitionService requisitionService;
    private final ProcessHistoryService processHistoryService;
    private final ApprovalRepository approvalRepository;
    private final AccountabilityReportRepository accountabilityReportRepository;
    private final PaymentDeclarationRepository paymentDeclarationRepository;
    private final ApprovalMapper approvalMapper;

    public ApprovalResponseDTO create(CreateApprovalDTO dto) {
        Approval approval = approvalMapper.toEntity(dto);

        approval.setApprovalStatus(ApprovalStatus.PENDENTE);

        return approvalMapper.toDTO(approvalRepository.save(approval));
    }

    public Page<ApprovalResponseDTO> findAll(Pageable pageable) {
        return approvalRepository.findAll(pageable).map(approvalMapper::toDTO);
    }

    public ApprovalResponseDTO findById(UUID id) {
        Approval approval = approvalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Approval not found"));

        return approvalMapper.toDTO(approval);
    }

    // apenas ADM
    public ApprovalResponseDTO update(UUID id, UpdateApprovalDTO dto, Employee employee) {

        Approval approval = approvalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Approval not found"));

        approval.setApprovalStatus(dto.status());
        approval.setApprovedBy(employee);
        approval.setObservation(dto.observation());

        return approvalMapper.toDTO(approvalRepository.save(approval));
    }

    public ApprovalResponseDTO processApproval(UUID id, UpdateApprovalDTO dto, Employee employee) {

        Approval approval = approvalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Approval not found"));

        validateProcurementResponsible(employee, approval);

        validateApprovalPermission(employee, approval.getApprovalSector());

        approval.setApprovalStatus(dto.status());
        approval.setApprovedBy(employee);
        approval.setObservation(dto.observation());
        approval.setApprovedAt(LocalDateTime.now());

        approval = approvalRepository.save(approval);

        Requisition requisition = approval.getRequisition();

        ProcessStatus processStatus = requisition.getProcessStatus();

        ProcessStage currentStage = processStatus.getStage();

        validateApprovalMatchesCurrentStage(approval, currentStage);
        updatePaymentDeclarationApproval(requisition, approval, employee);
        updateAccountabilityApproval(requisition, approval);

        if (dto.status() == ApprovalStatus.APROVADO) {

            processHistoryService.createProcessHistory(requisition, employee, "Etapa aprovada: " + currentStage.getDescription(), currentStage, HistoryEventType.APPROVED);

            if (currentStage == ProcessStage.HOMOLOGACAO_PRESTACAO_CONTAS) {
                requisitionService.finishWorkflow(requisition, employee, true);
            } else {
                ProcessStage nextStage = getNextStage(currentStage);
                requisitionService.sendToNextStage(requisition, nextStage, employee, "Processo enviado para " + nextStage.getDescription());
            }
        }

        if (dto.status() == ApprovalStatus.REPROVADO) {

            processHistoryService.createProcessHistory(requisition, employee, "Etapa reprovada: " + currentStage.getDescription(), currentStage, HistoryEventType.REJECTED);

            if (currentStage == ProcessStage.HOMOLOGACAO_PRESTACAO_CONTAS) {
                requisitionService.finishWorkflow(requisition, employee, false);
            }
        }

        return approvalMapper.toDTO(approval);
    }

    private void updatePaymentDeclarationApproval(
            Requisition requisition,
            Approval approval,
            Employee employee
    ) {
        if (approval.getApprovalSector() != ApprovalSector.DECLARACAO_PAGAMENTO) {
            return;
        }

        PaymentDeclaration declaration = paymentDeclarationRepository
                .findFirstByCommitmentContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisition.getId())
                .orElseThrow(() -> new BusinessException("Payment declaration must be issued before approval"));

        declaration.setSecretaryApproved(approval.getApprovalStatus() == ApprovalStatus.APROVADO);
        declaration.setApprovedBy(employee);
        paymentDeclarationRepository.save(declaration);
    }

    private void updateAccountabilityApproval(Requisition requisition, Approval approval) {
        if (approval.getApprovalSector() != ApprovalSector.PRESTACAO_CONTAS) {
            return;
        }

        AccountabilityReport report = accountabilityReportRepository
                .findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisition.getId())
                .orElseThrow(() -> new BusinessException("Prestação de contas não encontrada para homologação"));

        report.setStatus(
                approval.getApprovalStatus() == ApprovalStatus.APROVADO
                        ? AccountabilityStatus.APPROVED
                        : AccountabilityStatus.REJECTED
        );
        report.setAnalyzedAt(java.time.LocalDate.now());
        accountabilityReportRepository.save(report);
    }

    public Page<ApprovalResponseDTO> findPending(Pageable pageable) {

        return approvalRepository.findByApprovalStatus(ApprovalStatus.PENDENTE, pageable).map(approvalMapper::toDTO);
    }

    public ApprovalResponseDTO findPendingByRequisition(UUID requisitionId) {

        Approval approval = approvalRepository
                .findFirstByRequisitionIdAndApprovalStatusOrderByCreatedAtDesc(requisitionId, ApprovalStatus.PENDENTE)
                .orElseThrow(() -> new ResourceNotFoundException("Pending approval not found for requisition: " + requisitionId));

        return approvalMapper.toDTO(approval);
    }

    private void validateApprovalPermission(Employee employee, ApprovalSector sector) {

        if (sector == ApprovalSector.ANALISE_COMPRAS) {
            return;
        }

        String requiredPermission = switch (sector) {

            case REQUISICAO_SECRETARIO -> "approval.secretary";

            case ANALISE_COMPRAS -> "approval.procurement";

            case DECLARACAO_PAGAMENTO -> "approval.payment";

            case PRESTACAO_CONTAS -> "approval.accountability";
        };

        boolean hasPermission = employee.getOccupationId()
                .getPermissions()
                .stream()
                .map(relation -> relation.getPk().getPermission())
                .anyMatch(permission ->
                        permission.getResource().equals(requiredPermission)
                                && permission.getAction().contains(Actions.UPDATE)
                );

        if (!hasPermission) {

            throw new BusinessException("User does not have permission to approve this stage");
        }
    }

    private void validateProcurementResponsible(Employee employee, Approval approval) {

        if (approval.getApprovalSector() != ApprovalSector.ANALISE_COMPRAS) {
            return;
        }

        Employee responsible = approval.getRequisition().getProcurementResponsible();

        if (responsible == null || !responsible.getId().equals(employee.getId())) {
            throw new BusinessException("Only the assigned procurement employee can approve this stage");
        }
    }

    // apenas ADM
    public void delete(UUID id) {
        Approval approval = approvalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Approval not found"));
        approvalRepository.delete(approval);
    }

    private ProcessStage getNextStage(ProcessStage currentStage) {

        return Arrays.stream(ProcessStage.values()).filter(stage -> stage.getStep() == currentStage.getStep() + 1).findFirst().orElseThrow(() -> new BusinessException("Next stage not found"));
    }

    private void validateApprovalMatchesCurrentStage(Approval approval, ProcessStage currentStage) {

        ApprovalSector expectedSector = switch (currentStage) {

            case HOMOLOGACAO_SECRETARIO -> ApprovalSector.REQUISICAO_SECRETARIO;

            case HOMOLOGACAO_COMPRAS -> ApprovalSector.ANALISE_COMPRAS;

            case DECLARACAO_PAGAMENTO -> ApprovalSector.DECLARACAO_PAGAMENTO;

            case HOMOLOGACAO_PRESTACAO_CONTAS -> ApprovalSector.PRESTACAO_CONTAS;

            default -> null;
        };

        if (expectedSector == null || approval.getApprovalSector() != expectedSector) {
            throw new BusinessException("Approval does not belong to the current requisition stage");
        }
    }
}
