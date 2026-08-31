package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.paymentDeclaration.CreatePaymentDeclarationDTO;
import com.fiap.hackgov.bidding.internal.DTOs.paymentDeclaration.CreateRequisitionPaymentDeclarationDTO;
import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalSector;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.PaymentDeclarationMapper;
import com.fiap.hackgov.bidding.internal.repositories.ApprovalRepository;
import com.fiap.hackgov.bidding.internal.repositories.CommitmentRepository;
import com.fiap.hackgov.bidding.internal.repositories.PaymentDeclarationRepository;
import com.fiap.hackgov.bidding.internal.repositories.RequisitionRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentDeclarationService {

    private final PaymentDeclarationRepository paymentDeclarationRepository;
    private final CommitmentRepository commitmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ApprovalRepository approvalRepository;
    private final RequisitionRepository requisitionRepository;
    private final PaymentDeclarationMapper paymentDeclarationMapper;
    private final RequisitionService requisitionService;

    public PaymentDeclaration create(CreatePaymentDeclarationDTO dto) {
        Commitment commitment = findCommitment(dto.commitmentId());
        Employee approvedBy = findEmployee(dto.approvedById());

        PaymentDeclaration declaration = paymentDeclarationMapper.toEntity(dto);
        declaration.setCommitment(commitment);
        declaration.setApprovedBy(approvedBy);

        declaration = paymentDeclarationRepository.save(declaration);

        approvePaymentDeclarationIfNecessary(commitment, approvedBy, dto);

        return declaration;
    }

    public PaymentDeclaration createForRequisition(
            UUID requisitionId,
            CreateRequisitionPaymentDeclarationDTO dto,
            Employee employee
    ) {
        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisição não encontrada: " + requisitionId));

        validateRequisitionStage(requisition);
        validateIssuer(requisition, employee);

        paymentDeclarationRepository
                .findFirstByCommitmentContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .ifPresent(existing -> {
                    throw new BusinessException("A requisição já possui uma declaração para pagamento");
                });

        Commitment commitment = commitmentRepository
                .findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Empenho não encontrado para a requisição: " + requisitionId));

        PaymentDeclaration declaration = new PaymentDeclaration();
        declaration.setCommitment(commitment);
        declaration.setApprovedBy(employee);
        declaration.setType(dto.type());
        declaration.setDescription(dto.description());
        declaration.setSecretaryApproved(false);

        return paymentDeclarationRepository.save(declaration);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDeclaration> findAll(Pageable pageable) {
        return paymentDeclarationRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDeclaration> findByCommitmentId(UUID commitmentId, Pageable pageable) {
        findCommitment(commitmentId);
        return paymentDeclarationRepository.findAllByCommitmentId(commitmentId, pageable);
    }

    @Transactional(readOnly = true)
    public PaymentDeclaration findById(UUID id) {
        return paymentDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Declaração de pagamento não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public PaymentDeclaration findByRequisitionId(UUID requisitionId) {
        return paymentDeclarationRepository
                .findFirstByCommitmentContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Declaração de pagamento não encontrada para a requisição: " + requisitionId));
    }

    private Commitment findCommitment(UUID id) {
        return commitmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empenho não encontrado: " + id));
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
    }

    private void registerStage(Contract contract, Employee employee, ProcessStage stage, String observation) {
        requisitionService.sendToNextStage(contract.getLicitationProcess().getRequisition(), stage, employee, observation);
    }

    private void validateRequisitionStage(Requisition requisition) {
        if (requisition.getProcessStatus().getStage() != ProcessStage.DECLARACAO_PAGAMENTO) {
            throw new BusinessException("A declaração só pode ser emitida na etapa de declaração para pagamento");
        }
    }

    private void validateIssuer(Requisition requisition, Employee employee) {
        if (employee == null || !requisition.getResponsible().getId().equals(employee.getId())) {
            throw new BusinessException("Somente o servidor responsável pela requisição pode emitir a declaração para pagamento");
        }
    }

    private void approvePaymentDeclarationIfNecessary(Commitment commitment, Employee approvedBy, CreatePaymentDeclarationDTO dto) {
        if (!Boolean.TRUE.equals(dto.secretaryApproved())) {
            return;
        }

        UUID requisitionId = commitment.getContract().getLicitationProcess().getRequisition().getId();
        Approval approval = approvalRepository.findFirstByRequisitionIdAndApprovalSectorOrderByCreatedAtDesc(requisitionId, ApprovalSector.DECLARACAO_PAGAMENTO)
                .orElseThrow(() -> new ResourceNotFoundException("Aprovação da declaração de pagamento não encontrada"));

        approval.setApprovalStatus(ApprovalStatus.APROVADO);
        approval.setApprovedBy(approvedBy);
        approval.setApprovedAt(LocalDateTime.now());
        approval.setObservation("Declaração de pagamento aprovada pelo secretário");
        approvalRepository.save(approval);
    }
}
