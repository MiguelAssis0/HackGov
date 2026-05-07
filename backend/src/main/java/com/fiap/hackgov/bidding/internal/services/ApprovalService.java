package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.Approval.ApprovalResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Approval.CreateApprovalDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Approval.UpdateApprovalDTO;
import com.fiap.hackgov.bidding.internal.entities.Approval;
import com.fiap.hackgov.bidding.internal.mappers.ApprovalMapper;
import com.fiap.hackgov.bidding.internal.repositories.ApprovalRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.services.EmployeeService;
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
public class ApprovalService {

    private final EmployeeService employeeService;
    private final ApprovalRepository repository;
    private final ApprovalMapper mapper;

    public ApprovalResponseDTO create(CreateApprovalDTO dto) {
        Approval approval = mapper.toEntity(dto);

        approval.setApprovedAt(LocalDateTime.now());

        return mapper.toDTO(repository.save(approval));
    }

    public Page<ApprovalResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDTO);
    }

    public ApprovalResponseDTO findById(UUID id) {
        Approval approval = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found"));

        return mapper.toDTO(approval);
    }

    // Não sei se vai ser muito usado
    public ApprovalResponseDTO update(UUID id, UpdateApprovalDTO dto) {

        Approval approval = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found"));

        Employee employee = employeeService.findById(dto.approvedBy());

        approval.setApprovalStatus(dto.status());
        approval.setApprovedBy(employee);
        approval.setObservation(dto.observation());

        return mapper.toDTO(repository.save(approval));
    }


    public ApprovalResponseDTO processApproval(UUID id, UpdateApprovalDTO dto) {

        Approval approval = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found"));

        Employee employee = employeeService.findById(dto.approvedBy());

        approval.setApprovalStatus(dto.status());
        approval.setApprovedBy(employee);
        approval.setObservation(dto.observation());
        approval.setApprovedAt(LocalDateTime.now());

        return mapper.toDTO(repository.save(approval));
    }


    public void delete(UUID id) {
        Approval approval = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found"));

        repository.delete(approval);
    }
}