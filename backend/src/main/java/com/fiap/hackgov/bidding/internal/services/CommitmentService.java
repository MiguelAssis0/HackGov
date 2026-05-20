package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.commitment.CreateCommitmentDTO;
import com.fiap.hackgov.bidding.internal.entities.Commitment;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import com.fiap.hackgov.bidding.internal.entities.ExecutionOrder;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.HistoryEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.CommitmentMapper;
import com.fiap.hackgov.bidding.internal.repositories.CommitmentRepository;
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
public class CommitmentService {

    private final CommitmentRepository commitmentRepository;
    private final ContractRepository contractRepository;
    private final ExecutionOrderRepository executionOrderRepository;
    private final EmployeeRepository employeeRepository;
    private final CommitmentMapper commitmentMapper;
    private final RequisitionService requisitionService;
    private final ProcessHistoryService processHistoryService;

    public Commitment create(CreateCommitmentDTO dto) {
        Contract contract = findContract(dto.contractId());
        ExecutionOrder executionOrder = findExecutionOrder(dto.executionOrderId());
        Employee issuedBy = findEmployee(dto.issuedById());

        if (commitmentRepository.existsByCommitmentNumber(dto.commitmentNumber())) {
            throw new BusinessException("Número do empenho já existe");
        }

        validateExecutionOrderContract(contract, executionOrder);

        Commitment commitment = commitmentMapper.toEntity(dto);
        commitment.setContract(contract);
        commitment.setExecutionOrder(executionOrder);
        commitment.setIssuedBy(issuedBy);

        commitment = commitmentRepository.save(commitment);

        registerStage(contract, issuedBy, ProcessStage.EMISSAO_EMPENHO, "Empenho emitido: " + dto.commitmentNumber());

        return commitment;
    }

    @Transactional(readOnly = true)
    public Page<Commitment> findAll(Pageable pageable) {
        return commitmentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Commitment> findByContractId(UUID contractId, Pageable pageable) {
        findContract(contractId);
        return commitmentRepository.findAllByContractId(contractId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Commitment> findByExecutionOrderId(UUID executionOrderId, Pageable pageable) {
        findExecutionOrder(executionOrderId);
        return commitmentRepository.findAllByExecutionOrderId(executionOrderId, pageable);
    }

    @Transactional(readOnly = true)
    public Commitment findById(UUID id) {
        return commitmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empenho não encontrado: " + id));
    }

    private Contract findContract(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado: " + id));
    }

    private ExecutionOrder findExecutionOrder(UUID id) {
        if (id == null) {
            return null;
        }

        return executionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de execução não encontrada: " + id));
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
    }

    private void validateExecutionOrderContract(Contract contract, ExecutionOrder executionOrder) {
        if (executionOrder != null && !executionOrder.getContract().getId().equals(contract.getId())) {
            throw new BusinessException("A ordem de execução não pertence ao contrato informado");
        }
    }

    private void registerStage(Contract contract, Employee employee, ProcessStage stage, String observation) {
        Requisition requisition = contract.getLicitationProcess().getRequisition();
        requisitionService.updateCurrentStage(requisition.getProcessStatus(), stage, employee, observation);
        processHistoryService.createProcessHistory(requisition, employee, observation, stage, HistoryEventType.STAGE_SENT);
    }
}
