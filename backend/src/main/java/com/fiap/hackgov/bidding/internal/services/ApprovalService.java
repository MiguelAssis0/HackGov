package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.approval.ApprovalResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.approval.CreateApprovalDTO;
import com.fiap.hackgov.bidding.internal.DTOs.approval.UpdateApprovalDTO;
import com.fiap.hackgov.bidding.internal.entities.Approval;
import com.fiap.hackgov.bidding.internal.entities.ProcessStatus;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.HistoryEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.ApprovalMapper;
import com.fiap.hackgov.bidding.internal.repositories.ApprovalRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
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
    private final ApprovalMapper approvalMapper;

    public ApprovalResponseDTO create(CreateApprovalDTO dto) {
        Approval approval = approvalMapper.toEntity(dto);

        approval.setApprovedAt(LocalDateTime.now());

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

        approval.setApprovalStatus(dto.status());
        approval.setApprovedBy(employee);
        approval.setObservation(dto.observation());
        approval.setApprovedAt(LocalDateTime.now());

        approval = approvalRepository.save(approval);

        Requisition requisition = approval.getRequisition();

        ProcessStatus processStatus = requisition.getProcessStatus();

        ProcessStage currentStage = processStatus.getStage();

        if (dto.status() == ApprovalStatus.APROVADO) {

            ProcessStage approvedStage = getNextStage(currentStage);

            processStatus.setStage(approvedStage);
            processStatus.setResponsibleId(employee.getId());
            processStatus.setObservation(dto.observation());
            processStatus.setFinishedAt(LocalDateTime.now());

            processHistoryService.createProcessHistory(requisition, employee, "Etapa aprovada: " + approvedStage.getDescription(), approvedStage, HistoryEventType.APPROVED);

            ProcessStage nextStage = getNextStage(approvedStage);

            requisitionService.sendToNextStage(requisition, nextStage, employee, "Processo enviado para " + nextStage.getDescription());
        }

        if (dto.status() == ApprovalStatus.REPROVADO) {

            processHistoryService.createProcessHistory(requisition, employee, "Etapa reprovada: " + currentStage.getDescription(), currentStage, HistoryEventType.REJECTED);
        }

        return approvalMapper.toDTO(approval);
    }

    // apenas ADM
    public void delete(UUID id) {
        Approval approval = approvalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Approval not found"));
        approvalRepository.delete(approval);
    }

    private ProcessStage getNextStage(ProcessStage currentStage) {

        return Arrays.stream(ProcessStage.values()).filter(stage -> stage.getStep() == currentStage.getStep() + 1).findFirst().orElseThrow(() -> new BusinessException("Next stage not found"));
    }
}