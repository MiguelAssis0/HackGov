package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.accountabilityReport.AssignAccountabilityResponsibleDTO;
import com.fiap.hackgov.bidding.internal.DTOs.accountabilityReport.CreateAccountabilityReportDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.RequisitionResponsibleDTO;
import com.fiap.hackgov.bidding.internal.entities.AccountabilityReport;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import com.fiap.hackgov.bidding.internal.entities.Payment;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.AccountabilityStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.AccountabilityReportMapper;
import com.fiap.hackgov.bidding.internal.repositories.*;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountabilityReportService {

    private final AccountabilityReportRepository accountabilityReportRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final RequisitionRepository requisitionRepository;
    private final ProcessStatusRepository processStatusRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountabilityReportMapper accountabilityReportMapper;
    private final RequisitionService requisitionService;

    public AccountabilityReport create(CreateAccountabilityReportDTO dto) {
        Contract contract = findContract(dto.contractId());
        Employee responsible = findEmployee(dto.responsibleId());

        validateContractHasPayment(contract);

        AccountabilityReport accountability = accountabilityReportMapper.toEntity(dto);
        accountability.setContract(contract);
        accountability.setResponsible(responsible);
        accountability.setAnalyzedAt(resolveAnalyzedAt(dto));

        accountability = accountabilityReportRepository.save(accountability);

        registerStage(contract, responsible, ProcessStage.PRESTACAO_CONTAS, "Prestação de contas registrada");

        return accountability;
    }

    public AccountabilityReport assignResponsible(
            UUID requisitionId,
            AssignAccountabilityResponsibleDTO dto,
            Employee assignedBy
    ) {
        Requisition requisition = findRequisition(requisitionId);
        validateRequisitionStage(requisition);

        if (assignedBy == null) {
            throw new BusinessException("Usuário autenticado não identificado");
        }

        accountabilityReportRepository
                .findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .ifPresent(existing -> {
                    throw new BusinessException("A requisição já possui um responsável pela prestação de contas");
                });

        Employee responsible = findEligibleEmployeesEntities(requisition).stream()
                .filter(employee -> employee.getId().equals(dto.employeeId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("O responsável deve pertencer à área de prestação de contas"));

        Payment payment = paymentRepository
                .findFirstByDeclarationCommitmentContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para a requisição: " + requisitionId));

        AccountabilityReport accountability = new AccountabilityReport();
        accountability.setContract(payment.getDeclaration().getCommitment().getContract());
        accountability.setResponsible(responsible);
        accountability.setStatus(AccountabilityStatus.UNDER_REVIEW);
        accountability.setObservation(dto.observation());
        accountability.setAnalyzedAt(null);
        accountability = accountabilityReportRepository.save(accountability);

        requisitionService.sendToNextStage(
                requisition,
                ProcessStage.ANALISE_PRESTACAO_CONTAS,
                assignedBy,
                "Responsável pela prestação de contas definido: " + responsible.getFullName()
        );

        requisition.getProcessStatus().setResponsibleId(responsible.getId());
        processStatusRepository.save(requisition.getProcessStatus());

        return accountability;
    }

    @Transactional(readOnly = true)
    public Page<AccountabilityReport> findAll(Pageable pageable) {
        return accountabilityReportRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AccountabilityReport> findByContractId(UUID contractId, Pageable pageable) {
        findContract(contractId);
        return accountabilityReportRepository.findAllByContractId(contractId, pageable);
    }

    @Transactional(readOnly = true)
    public AccountabilityReport findById(UUID id) {
        return accountabilityReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestação de contas não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public AccountabilityReport findByRequisitionId(UUID requisitionId) {
        return accountabilityReportRepository
                .findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestação de contas não encontrada para a requisição: " + requisitionId));
    }

    @Transactional(readOnly = true)
    public List<RequisitionResponsibleDTO> findEligibleEmployees(UUID requisitionId) {
        Requisition requisition = findRequisition(requisitionId);

        return findEligibleEmployeesEntities(requisition).stream()
                .map(employee -> new RequisitionResponsibleDTO(employee.getId(), employee.getFullName()))
                .toList();
    }

    private Contract findContract(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado: " + id));
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
    }

    private Requisition findRequisition(UUID id) {
        return requisitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requisição não encontrada: " + id));
    }

    private List<Employee> findEligibleEmployeesEntities(Requisition requisition) {
        return employeeRepository.findActiveAccountabilityEmployees(requisition.getSector().getCityHall().getId());
    }

    private void validateRequisitionStage(Requisition requisition) {
        if (requisition.getProcessStatus().getStage() != ProcessStage.PRESTACAO_CONTAS) {
            throw new BusinessException("O responsável só pode ser definido na etapa de prestação de contas");
        }
    }

    private void validateContractHasPayment(Contract contract) {
        if (!paymentRepository.existsByDeclarationCommitmentContractId(contract.getId())) {
            throw new BusinessException("A prestação de contas só pode ser criada após pelo menos um pagamento do contrato");
        }
    }

    private LocalDate resolveAnalyzedAt(CreateAccountabilityReportDTO dto) {
        if (dto.analyzedAt() != null) {
            return dto.analyzedAt();
        }

        if (dto.status() == AccountabilityStatus.APPROVED || dto.status() == AccountabilityStatus.REJECTED) {
            return LocalDate.now();
        }

        return null;
    }

    private void registerStage(Contract contract, Employee employee, ProcessStage stage, String observation) {
        requisitionService.sendToNextStage(contract.getLicitationProcess().getRequisition(), stage, employee, observation);
    }
}
