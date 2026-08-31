package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.licitation.*;
import com.fiap.hackgov.bidding.internal.entities.LicitationHistory;
import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import com.fiap.hackgov.bidding.internal.mappers.LicitationHistoryMapper;
import com.fiap.hackgov.bidding.internal.mappers.LicitationProcessMapper;
import com.fiap.hackgov.bidding.internal.services.LicitationProcessService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/licitation-processes")
@RequiredArgsConstructor
public class LicitationProcessController {

    private final LicitationProcessService licitationProcessService;
    private final LicitationProcessMapper licitationProcessMapper;
    private final LicitationHistoryMapper licitationHistoryMapper;
    private final PaginationMapper paginationMapper;

    @PostMapping
    public ResponseEntity<LicitationProcessResponseDTO> create(@Valid @RequestBody CreateLicitationProcessDTO dto, @AuthenticationPrincipal Employee employee) {

        LicitationProcess licitationProcess = licitationProcessService.create(dto, employee);

        return ResponseEntity.status(HttpStatus.CREATED).body(licitationProcessMapper.toDTO(licitationProcess));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<LicitationProcessResponseDTO>> findAll(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LicitationProcess> processes = licitationProcessService.findAll(pageable);

        Page<LicitationProcessResponseDTO> dtoPage = processes.map(licitationProcessMapper::toDTO);

        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LicitationProcessResponseDTO> findById(@PathVariable UUID id) {

        LicitationProcess licitationProcess = licitationProcessService.findById(id);

        return ResponseEntity.ok(licitationProcessMapper.toDTO(licitationProcess));
    }

    @GetMapping("/requisition/{requisitionId}")
    public ResponseEntity<LicitationProcessResponseDTO> findByRequisitionId(@PathVariable UUID requisitionId) {

        LicitationProcess licitationProcess = licitationProcessService.findByRequisitionId(requisitionId);

        return ResponseEntity.ok(licitationProcessMapper.toDTO(licitationProcess));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LicitationProcessResponseDTO> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateLicitationStatusDTO dto, @AuthenticationPrincipal Employee employee) {

        LicitationProcess licitationProcess = licitationProcessService.updateStatus(id, dto.status(), dto.observation(), employee);

        return ResponseEntity.ok(licitationProcessMapper.toDTO(licitationProcess));
    }

    @PatchMapping("/{id}/result")
    public ResponseEntity<LicitationProcessResponseDTO> publishResult(
            @PathVariable UUID id,
            @Valid @RequestBody PublishLicitationResultDTO dto,
            @AuthenticationPrincipal Employee employee
    ) {

        LicitationProcess licitationProcess = licitationProcessService.publishResult(id, dto, employee);

        return ResponseEntity.ok(licitationProcessMapper.toDTO(licitationProcess));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<LicitationHistoryDTO>> getHistory(@PathVariable UUID id) {

        List<LicitationHistory> history = licitationProcessService.getHistory(id);

        return ResponseEntity.ok(licitationHistoryMapper.toDTOList(history));
    }
}
