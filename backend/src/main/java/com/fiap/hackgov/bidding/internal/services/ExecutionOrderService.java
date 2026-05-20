package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.executionOrder.CreateExecutionOrderDTO;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import com.fiap.hackgov.bidding.internal.entities.ExecutionOrder;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.ContractStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.HistoryEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.ExecutionOrderMapper;
import com.fiap.hackgov.bidding.internal.repositories.ContractRepository;
import com.fiap.hackgov.bidding.internal.repositories.ExecutionOrderRepository;
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
    private final EmployeeRepository employeeRepository;
    private final ExecutionOrderMapper executionOrderMapper;
    private final RequisitionService requisitionService;
    private final ProcessHistoryService processHistoryService;

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

        registerStage(contract, issuedBy, ProcessStage.INICIO_SERVICOS, "Ordem de execução emitida: " + dto.number());

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

    private void registerStage(Contract contract, Employee employee, ProcessStage stage, String observation) {
        Requisition requisition = contract.getLicitationProcess().getRequisition();
        requisitionService.updateCurrentStage(requisition.getProcessStatus(), stage, employee, observation);
        processHistoryService.createProcessHistory(requisition, employee, observation, stage, HistoryEventType.STAGE_SENT);
    }
}
