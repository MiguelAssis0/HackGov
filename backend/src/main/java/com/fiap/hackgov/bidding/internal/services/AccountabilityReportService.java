package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.accountabilityReport.CreateAccountabilityReportDTO;
import com.fiap.hackgov.bidding.internal.entities.AccountabilityReport;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.AccountabilityStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.HistoryEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.AccountabilityReportMapper;
import com.fiap.hackgov.bidding.internal.repositories.AccountabilityReportRepository;
import com.fiap.hackgov.bidding.internal.repositories.ContractRepository;
import com.fiap.hackgov.bidding.internal.repositories.PaymentRepository;
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
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountabilityReportService {

    private final AccountabilityReportRepository accountabilityReportRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountabilityReportMapper accountabilityReportMapper;
    private final RequisitionService requisitionService;
    private final ProcessHistoryService processHistoryService;

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

    private Contract findContract(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado: " + id));
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
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
        Requisition requisition = contract.getLicitationProcess().getRequisition();
        requisitionService.updateCurrentStage(requisition.getProcessStatus(), stage, employee, observation);
        processHistoryService.createProcessHistory(requisition, employee, observation, stage, HistoryEventType.STAGE_SENT);
    }
}
