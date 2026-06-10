package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.commitment.CreateCommitmentDTO;
import com.fiap.hackgov.bidding.internal.DTOs.commitment.CreateRequisitionCommitmentDTO;
import com.fiap.hackgov.bidding.internal.entities.Commitment;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import com.fiap.hackgov.bidding.internal.entities.ExecutionOrder;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.CommitmentMapper;
import com.fiap.hackgov.bidding.internal.repositories.CommitmentRepository;
import com.fiap.hackgov.bidding.internal.repositories.ContractRepository;
import com.fiap.hackgov.bidding.internal.repositories.ExecutionOrderRepository;
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
public class CommitmentService {

    private final CommitmentRepository commitmentRepository;
    private final ContractRepository contractRepository;
    private final ExecutionOrderRepository executionOrderRepository;
    private final RequisitionRepository requisitionRepository;
    private final EmployeeRepository employeeRepository;
    private final CommitmentMapper commitmentMapper;
    private final RequisitionService requisitionService;

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

        registerStage(contract, issuedBy, ProcessStage.DECLARACAO_PAGAMENTO, "Empenho emitido: " + dto.commitmentNumber());

        return commitment;
    }

    public Commitment createForRequisition(
            UUID requisitionId,
            CreateRequisitionCommitmentDTO dto,
            Employee employee
    ) {
        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisição não encontrada: " + requisitionId));

        validateRequisitionStage(requisition);
        validateIssuer(requisition, employee);

        if (commitmentRepository.existsByCommitmentNumber(dto.commitmentNumber())) {
            throw new BusinessException("Número do empenho já existe");
        }

        commitmentRepository.findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .ifPresent(existing -> {
                    throw new BusinessException("A requisição já possui um empenho");
                });

        ExecutionOrder executionOrder = executionOrderRepository
                .findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de execução não encontrada para a requisição: " + requisitionId));

        Commitment commitment = new Commitment();
        commitment.setContract(executionOrder.getContract());
        commitment.setExecutionOrder(executionOrder);
        commitment.setIssuedBy(employee);
        commitment.setType(dto.type());
        commitment.setCommitmentNumber(dto.commitmentNumber());
        commitment.setReservedValue(dto.reservedValue());

        commitment = commitmentRepository.save(commitment);

        registerStage(
                executionOrder.getContract(),
                employee,
                ProcessStage.DECLARACAO_PAGAMENTO,
                "Empenho emitido: " + dto.commitmentNumber()
        );

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

    @Transactional(readOnly = true)
    public Commitment findByRequisitionId(UUID requisitionId) {
        return commitmentRepository.findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Empenho não encontrado para a requisição: " + requisitionId));
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

    private void validateRequisitionStage(Requisition requisition) {
        if (requisition.getProcessStatus().getStage() != ProcessStage.EMISSAO_EMPENHO) {
            throw new BusinessException("O empenho só pode ser emitido na etapa de emissão de empenho");
        }
    }

    private void validateIssuer(Requisition requisition, Employee employee) {
        if (employee == null || !requisition.getResponsible().getId().equals(employee.getId())) {
            throw new BusinessException("Somente o servidor responsável pela requisição pode emitir o empenho");
        }
    }

    private void registerStage(Contract contract, Employee employee, ProcessStage stage, String observation) {
        requisitionService.sendToNextStage(contract.getLicitationProcess().getRequisition(), stage, employee, observation);
    }
}
