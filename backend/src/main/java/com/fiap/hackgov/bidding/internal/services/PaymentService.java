package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.payment.CreatePaymentDTO;
import com.fiap.hackgov.bidding.internal.DTOs.payment.CreateRequisitionPaymentDTO;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import com.fiap.hackgov.bidding.internal.entities.Payment;
import com.fiap.hackgov.bidding.internal.entities.PaymentDeclaration;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.PaymentMapper;
import com.fiap.hackgov.bidding.internal.repositories.PaymentDeclarationRepository;
import com.fiap.hackgov.bidding.internal.repositories.PaymentRepository;
import com.fiap.hackgov.bidding.internal.repositories.RequisitionRepository;
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

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDeclarationRepository paymentDeclarationRepository;
    private final RequisitionRepository requisitionRepository;
    private final EmployeeRepository employeeRepository;
    private final SectorRepository sectorRepository;
    private final PaymentMapper paymentMapper;
    private final RequisitionService requisitionService;

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

        registerStage(declaration.getCommitment().getContract(), approvedBy, ProcessStage.PRESTACAO_CONTAS, "Pagamento executado");

        return payment;
    }

    public Payment createForRequisition(
            UUID requisitionId,
            CreateRequisitionPaymentDTO dto,
            Employee authenticatedEmployee
    ) {
        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisição não encontrada: " + requisitionId));

        validateRequisitionStage(requisition);

        Employee treasuryResponsible = findEmployeeWithDetails(authenticatedEmployee);
        validateTreasuryResponsible(requisition, treasuryResponsible);

        paymentRepository
                .findFirstByDeclarationCommitmentContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .ifPresent(existing -> {
                    throw new BusinessException("A requisição já possui um pagamento executado");
                });

        PaymentDeclaration declaration = paymentDeclarationRepository
                .findFirstByCommitmentContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Declaração de pagamento não encontrada para a requisição: " + requisitionId));

        if (!Boolean.TRUE.equals(declaration.getSecretaryApproved())) {
            throw new BusinessException("A declaração de pagamento precisa ser aprovada pelo secretário antes do pagamento");
        }

        Payment payment = new Payment();
        payment.setDeclaration(declaration);
        payment.setValue(dto.value());
        payment.setTreasuryApproved(true);
        payment.setTreasuryResponsible(treasuryResponsible);
        payment.setTreasurySector(treasuryResponsible.getSectorId());
        payment.setApprovedBy(treasuryResponsible);
        payment.setPaidAt(dto.paidAt());

        payment = paymentRepository.save(payment);

        requisitionService.sendToNextStage(
                requisition,
                ProcessStage.PRESTACAO_CONTAS,
                treasuryResponsible,
                "Pagamento executado no valor de " + dto.value()
        );

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

    @Transactional(readOnly = true)
    public Payment findByRequisitionId(UUID requisitionId) {
        return paymentRepository
                .findFirstByDeclarationCommitmentContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para a requisição: " + requisitionId));
    }

    private PaymentDeclaration findDeclaration(UUID id) {
        return paymentDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Declaração de pagamento não encontrada: " + id));
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
    }

    private Employee findEmployeeWithDetails(Employee authenticatedEmployee) {
        if (authenticatedEmployee == null) {
            throw new BusinessException("Usuário autenticado não identificado");
        }

        return employeeRepository.findByIdWithDetails(authenticatedEmployee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + authenticatedEmployee.getId()));
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

    private void validateRequisitionStage(Requisition requisition) {
        if (requisition.getProcessStatus().getStage() != ProcessStage.EXECUCAO_PAGAMENTO) {
            throw new BusinessException("O pagamento só pode ser executado na etapa de execução do pagamento");
        }
    }

    private void validateTreasuryResponsible(Requisition requisition, Employee employee) {
        if (employee.getCityHallId() == null
                || !employee.getCityHallId().getId().equals(requisition.getSector().getCityHall().getId())) {
            throw new BusinessException("O responsável pelo pagamento deve pertencer à prefeitura da requisição");
        }

        if (employee.getSectorId() == null) {
            throw new BusinessException("O responsável pelo pagamento deve pertencer ao setor financeiro");
        }

        String sectorName = employee.getSectorId().getName().toLowerCase(Locale.ROOT);
        boolean treasurySector = sectorName.contains("fazenda")
                || sectorName.contains("finance")
                || sectorName.contains("tesour");

        if (!treasurySector) {
            throw new BusinessException("Somente um servidor da Fazenda pode executar o pagamento");
        }

        if (!Boolean.TRUE.equals(employee.getStatus())) {
            throw new BusinessException("O responsável pelo pagamento deve estar ativo");
        }
    }

    private void registerStage(Contract contract, Employee employee, ProcessStage stage, String observation) {
        requisitionService.sendToNextStage(contract.getLicitationProcess().getRequisition(), stage, employee, observation);
    }
}
