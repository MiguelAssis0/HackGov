package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.executionOrder.CreateExecutionOrderDTO;
import com.fiap.hackgov.bidding.internal.DTOs.executionOrder.ExecutionOrderResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.ExecutionOrder;
import com.fiap.hackgov.bidding.internal.mappers.ExecutionOrderMapper;
import com.fiap.hackgov.bidding.internal.services.ExecutionOrderService;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/execution-orders")
@RequiredArgsConstructor
public class ExecutionOrderController {

    private final ExecutionOrderService executionOrderService;
    private final ExecutionOrderMapper executionOrderMapper;
    private final PaginationMapper paginationMapper;

    @PostMapping
    public ResponseEntity<ExecutionOrderResponseDTO> create(@Valid @RequestBody CreateExecutionOrderDTO dto) {
        ExecutionOrder executionOrder = executionOrderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(executionOrderMapper.toDTO(executionOrder));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<ExecutionOrderResponseDTO>> findAll(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ExecutionOrderResponseDTO> dtoPage = executionOrderService.findAll(pageable).map(executionOrderMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionOrderResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(executionOrderMapper.toDTO(executionOrderService.findById(id)));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<PageResponseDTO<ExecutionOrderResponseDTO>> findByContractId(
            @PathVariable UUID contractId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ExecutionOrderResponseDTO> dtoPage = executionOrderService.findByContractId(contractId, pageable).map(executionOrderMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }
}
