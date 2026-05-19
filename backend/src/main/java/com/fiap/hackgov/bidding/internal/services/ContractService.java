package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.contract.CreateContractDTO;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import com.fiap.hackgov.bidding.internal.entities.Supplier;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationStatus;
import com.fiap.hackgov.bidding.internal.mappers.ContractMapper;
import com.fiap.hackgov.bidding.internal.repositories.ContractRepository;
import com.fiap.hackgov.bidding.internal.repositories.LicitationProcessRepository;
import com.fiap.hackgov.bidding.internal.repositories.SupplierRepository;
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
public class ContractService {

    private final ContractRepository contractRepository;
    private final LicitationProcessRepository licitationProcessRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final LicitationProcessService licitationProcessService;
    private final ContractMapper contractMapper;

    public Contract create(CreateContractDTO dto) {

        LicitationProcess licitationProcess = licitationProcessRepository.findById(dto.licitationProcessId())
                .orElseThrow(() -> new ResourceNotFoundException("Licitation process not found: " + dto.licitationProcessId()));

        Supplier supplier = supplierRepository.findById(dto.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + dto.supplierId()));

        Employee responsible = employeeRepository.findById(dto.responsibleId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.responsibleId()));

        contractRepository.findByLicitationProcessId(licitationProcess.getId()).ifPresent(existing -> {
            throw new BusinessException("Licitation process already has a contract");
        });

        if (contractRepository.existsByContractNumber(dto.contractNumber())) {
            throw new BusinessException("Contract number already exists");
        }

        validateDates(dto);
        validateSupplier(licitationProcess, supplier);

        Contract contract = contractMapper.toEntity(dto);
        contract.setLicitationProcess(licitationProcess);
        contract.setSupplier(supplier);
        contract.setResponsible(responsible);

        contract = contractRepository.save(contract);

        licitationProcessService.createHistory(
                licitationProcess,
                responsible,
                LicitationEventType.CONTRACT_CREATED,
                licitationProcess.getStatus(),
                "Contrato criado: " + dto.contractNumber()
        );

        return contract;
    }

    @Transactional(readOnly = true)
    public Page<Contract> findAll(Pageable pageable) {
        return contractRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Contract findById(UUID id) {
        return contractRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
    }

    @Transactional(readOnly = true)
    public Contract findByLicitationProcessId(UUID licitationProcessId) {
        return contractRepository.findByLicitationProcessId(licitationProcessId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found for licitation process: " + licitationProcessId));
    }

    private void validateDates(CreateContractDTO dto) {

        if (dto.startDate().isBefore(dto.signedAt())) {
            throw new BusinessException("Contract start date cannot be before signed date");
        }

        if (dto.endDate().isBefore(dto.startDate())) {
            throw new BusinessException("Contract end date cannot be before start date");
        }
    }

    private void validateSupplier(LicitationProcess licitationProcess, Supplier supplier) {

        if (licitationProcess.getWinnerSupplier() != null && !licitationProcess.getWinnerSupplier().getId().equals(supplier.getId())) {
            throw new BusinessException("Contract supplier must match the winning supplier");
        }

        if (licitationProcess.getStatus() != LicitationStatus.FINISHED) {
            throw new BusinessException("Contract can only be created for a finished licitation process");
        }
    }
}
