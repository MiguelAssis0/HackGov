package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.executionOrder.CreateExecutionOrderDTO;
import com.fiap.hackgov.bidding.internal.DTOs.executionOrder.CreateRequisitionExecutionOrderDTO;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import com.fiap.hackgov.bidding.internal.entities.ExecutionOrder;
import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.ContractStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.ExecutionOrderMapper;
import com.fiap.hackgov.bidding.internal.repositories.ContractRepository;
import com.fiap.hackgov.bidding.internal.repositories.ExecutionOrderRepository;
import com.fiap.hackgov.bidding.internal.repositories.LicitationProcessRepository;
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

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ExecutionOrderService {

    private final ExecutionOrderRepository executionOrderRepository;
    private final ContractRepository contractRepository;
    private final LicitationProcessRepository licitationProcessRepository;
    private final RequisitionRepository requisitionRepository;
    private final EmployeeRepository employeeRepository;
    private final ExecutionOrderMapper executionOrderMapper;
    private final RequisitionService requisitionService;

    public ExecutionOrder create(CreateExecutionOrderDTO dto) {
        Contract contract = findContract(dto.contractId());
        Employee issuedBy = findEmployee(dto.issuedById());

        if (executionOrderRepository.existsByNumber(dto.number())) {
            throw new BusinessException("Número da ordem de execução já existe");
        }

        validateContract(contract);

        ExecutionOrder executionOrder = executionOrderMapper.toEntity(dto);
        executionOrder.setContract(contract);
        executionOrder.setIssuedBy(issuedBy);

        executionOrder = executionOrderRepository.save(executionOrder);

        registerStage(contract, issuedBy, ProcessStage.EMISSAO_EMPENHO, "Ordem de execução emitida: " + dto.number());

        return executionOrder;
    }

    public ExecutionOrder createForRequisition(
            UUID requisitionId,
            CreateRequisitionExecutionOrderDTO dto,
            Employee employee
    ) {
        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisição não encontrada: " + requisitionId));

        validateRequisitionStage(requisition);
        validateIssuer(requisition, employee);

        if (executionOrderRepository.existsByNumber(dto.number())) {
            throw new BusinessException("Número da ordem de execução já existe");
        }

        executionOrderRepository.findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .ifPresent(existing -> {
                    throw new BusinessException("A requisição já possui uma ordem de execução");
                });

        LicitationProcess licitationProcess = licitationProcessRepository.findByRequisitionId(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo licitatório não encontrado para a requisição: " + requisitionId));

        validateFinishedLicitation(licitationProcess);

        Contract contract = contractRepository.findByLicitationProcessId(licitationProcess.getId())
                .orElseGet(() -> createTechnicalContract(licitationProcess, employee));

        ExecutionOrder executionOrder = new ExecutionOrder();
        executionOrder.setContract(contract);
        executionOrder.setIssuedBy(employee);
        executionOrder.setType(dto.type());
        executionOrder.setNumber(dto.number());
        executionOrder.setDescription(dto.description());
        executionOrder.setIssuedAt(dto.issuedAt());

        executionOrder = executionOrderRepository.save(executionOrder);

        registerStage(
                contract,
                employee,
                ProcessStage.EMISSAO_EMPENHO,
                "Ordem de execução emitida: " + dto.number()
        );

        return executionOrder;
    }

    @Transactional(readOnly = true)
    public Page<ExecutionOrder> findAll(Pageable pageable) {
        return executionOrderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ExecutionOrder> findByContractId(UUID contractId, Pageable pageable) {
        findContract(contractId);
        return executionOrderRepository.findAllByContractId(contractId, pageable);
    }

    @Transactional(readOnly = true)
    public ExecutionOrder findById(UUID id) {
        return executionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de execução não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public ExecutionOrder findByRequisitionId(UUID requisitionId) {
        return executionOrderRepository.findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de execução não encontrada para a requisição: " + requisitionId));
    }

    private Contract findContract(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado: " + id));
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
    }

    private void validateContract(Contract contract) {
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException("A ordem de execução só pode ser criada para um contrato ativo");
        }
    }

    private void validateRequisitionStage(Requisition requisition) {
        if (requisition.getProcessStatus().getStage() != ProcessStage.INICIO_SERVICOS) {
            throw new BusinessException("A ordem de execução só pode ser emitida na etapa de início dos serviços");
        }
    }

    private void validateIssuer(Requisition requisition, Employee employee) {
        if (employee == null || !requisition.getResponsible().getId().equals(employee.getId())) {
            throw new BusinessException("Somente o servidor responsável pela requisição pode emitir a ordem de execução");
        }
    }

    private void validateFinishedLicitation(LicitationProcess licitationProcess) {
        if (licitationProcess.getStatus() != LicitationStatus.FINISHED
                || licitationProcess.getWinnerSupplier() == null) {
            throw new BusinessException("A licitação deve estar finalizada e possuir empresa vencedora");
        }
    }

    private Contract createTechnicalContract(LicitationProcess licitationProcess, Employee responsible) {
        Contract contract = new Contract();
        contract.setLicitationProcess(licitationProcess);
        contract.setSupplier(licitationProcess.getWinnerSupplier());
        contract.setContractNumber(generateTechnicalContractNumber(licitationProcess));
        contract.setObjectDescription(licitationProcess.getObjectDescription());
        contract.setTotalValue(licitationProcess.getEstimatedValue());
        contract.setResponsible(responsible);
        contract.setStatus(ContractStatus.ACTIVE);
        return contractRepository.save(contract);
    }

    private String generateTechnicalContractNumber(LicitationProcess licitationProcess) {
        String processNumber = licitationProcess.getProcessNumber();
        String contractNumber = processNumber == null
                ? "CTR-AUTO-" + licitationProcess.getId()
                : processNumber.replaceFirst("^LIC-", "CTR-");

        if (contractRepository.existsByContractNumber(contractNumber)) {
            return "CTR-AUTO-" + licitationProcess.getId();
        }

        return contractNumber;
    }

    private void registerStage(Contract contract, Employee employee, ProcessStage stage, String observation) {
        requisitionService.sendToNextStage(contract.getLicitationProcess().getRequisition(), stage, employee, observation);
    }
}
