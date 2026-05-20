package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.payment.CreatePaymentDTO;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import com.fiap.hackgov.bidding.internal.entities.Payment;
import com.fiap.hackgov.bidding.internal.entities.PaymentDeclaration;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.HistoryEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.PaymentMapper;
import com.fiap.hackgov.bidding.internal.repositories.PaymentDeclarationRepository;
import com.fiap.hackgov.bidding.internal.repositories.PaymentRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDeclarationRepository paymentDeclarationRepository;
    private final EmployeeRepository employeeRepository;
    private final SectorRepository sectorRepository;
    private final PaymentMapper paymentMapper;
    private final RequisitionService requisitionService;
    private final ProcessHistoryService processHistoryService;

    public Payment create(CreatePaymentDTO dto) {
        PaymentDeclaration declaration = findDeclaration(dto.declarationId());
        Employee treasuryResponsible = findEmployee(dto.treasuryResponsibleId());
        Employee approvedBy = findEmployee(dto.approvedById());
        Sector treasurySector = findSector(dto.treasurySectorId());

        validateDeclaration(declaration, dto);

        Payment payment = paymentMapper.toEntity(dto);
        payment.setDeclaration(declaration);
        payment.setTreasuryResponsible(treasuryResponsible);
        payment.setTreasurySector(treasurySector);
        payment.setApprovedBy(approvedBy);

        payment = paymentRepository.save(payment);

        registerStage(declaration.getCommitment().getContract(), approvedBy, ProcessStage.EXECUCAO_PAGAMENTO, "Pagamento executado");

        return payment;
    }

    @Transactional(readOnly = true)
    public Page<Payment> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Payment> findByDeclarationId(UUID declarationId, Pageable pageable) {
        findDeclaration(declarationId);
        return paymentRepository.findAllByDeclarationId(declarationId, pageable);
    }

    @Transactional(readOnly = true)
    public Payment findById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: " + id));
    }

    private PaymentDeclaration findDeclaration(UUID id) {
        return paymentDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Declaração de pagamento não encontrada: " + id));
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
    }

    private Sector findSector(UUID id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado: " + id));
    }

    private void validateDeclaration(PaymentDeclaration declaration, CreatePaymentDTO dto) {
        if (!Boolean.TRUE.equals(declaration.getSecretaryApproved())) {
            throw new BusinessException("A declaração de pagamento precisa ser aprovada pelo secretário antes do pagamento");
        }

        if (!Boolean.TRUE.equals(dto.treasuryApproved())) {
            throw new BusinessException("O pagamento precisa ser aprovado pela fazenda antes da execução");
        }
    }

    private void registerStage(Contract contract, Employee employee, ProcessStage stage, String observation) {
        Requisition requisition = contract.getLicitationProcess().getRequisition();
        requisitionService.updateCurrentStage(requisition.getProcessStatus(), stage, employee, observation);
        processHistoryService.createProcessHistory(requisition, employee, observation, stage, HistoryEventType.STAGE_SENT);
    }
}
