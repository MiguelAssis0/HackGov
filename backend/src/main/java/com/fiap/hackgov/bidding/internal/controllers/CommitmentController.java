package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.commitment.CommitmentResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.commitment.CreateCommitmentDTO;
import com.fiap.hackgov.bidding.internal.DTOs.commitment.CreateRequisitionCommitmentDTO;
import com.fiap.hackgov.bidding.internal.entities.Commitment;
import com.fiap.hackgov.bidding.internal.mappers.CommitmentMapper;
import com.fiap.hackgov.bidding.internal.services.CommitmentService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.pagination.PageResponseDTO;
import com.fiap.hackgov.shared.infra.pagination.PaginationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/commitments")
@RequiredArgsConstructor
public class CommitmentController {

    private final CommitmentService commitmentService;
    private final CommitmentMapper commitmentMapper;
    private final PaginationMapper paginationMapper;

    @PostMapping
    public ResponseEntity<CommitmentResponseDTO> create(@Valid @RequestBody CreateCommitmentDTO dto) {
        Commitment commitment = commitmentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(commitmentMapper.toDTO(commitment));
    }

    @PostMapping("/requisition/{requisitionId}")
    public ResponseEntity<CommitmentResponseDTO> createForRequisition(@PathVariable UUID requisitionId, @Valid @RequestBody CreateRequisitionCommitmentDTO dto, @AuthenticationPrincipal Employee employee) {
        Commitment commitment = commitmentService.createForRequisition(requisitionId, dto, employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(commitmentMapper.toDTO(commitment));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<CommitmentResponseDTO>> findAll(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommitmentResponseDTO> dtoPage = commitmentService.findAll(pageable).map(commitmentMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommitmentResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(commitmentMapper.toDTO(commitmentService.findById(id)));
    }

    @GetMapping("/requisition/{requisitionId}")
    public ResponseEntity<CommitmentResponseDTO> findByRequisitionId(@PathVariable UUID requisitionId) {
        return ResponseEntity.ok(commitmentMapper.toDTO(commitmentService.findByRequisitionId(requisitionId)));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<PageResponseDTO<CommitmentResponseDTO>> findByContractId(@PathVariable UUID contractId, @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommitmentResponseDTO> dtoPage = commitmentService.findByContractId(contractId, pageable).map(commitmentMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }

    @GetMapping("/execution-order/{executionOrderId}")
    public ResponseEntity<PageResponseDTO<CommitmentResponseDTO>> findByExecutionOrderId(@PathVariable UUID executionOrderId, @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommitmentResponseDTO> dtoPage = commitmentService.findByExecutionOrderId(executionOrderId, pageable).map(commitmentMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }
}
