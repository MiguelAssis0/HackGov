package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.accountabilityReport.AccountabilityReportResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.accountabilityReport.CreateAccountabilityReportDTO;
import com.fiap.hackgov.bidding.internal.entities.AccountabilityReport;
import com.fiap.hackgov.bidding.internal.mappers.AccountabilityReportMapper;
import com.fiap.hackgov.bidding.internal.services.AccountabilityReportService;
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
@RequestMapping("/api/accountability-reports")
@RequiredArgsConstructor
public class AccountabilityReportController {

    private final AccountabilityReportService accountabilityReportService;
    private final AccountabilityReportMapper accountabilityReportMapper;
    private final PaginationMapper paginationMapper;

    @PostMapping
    public ResponseEntity<AccountabilityReportResponseDTO> create(@Valid @RequestBody CreateAccountabilityReportDTO dto) {
        AccountabilityReport accountability = accountabilityReportService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountabilityReportMapper.toDTO(accountability));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<AccountabilityReportResponseDTO>> findAll(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AccountabilityReportResponseDTO> dtoPage = accountabilityReportService.findAll(pageable).map(accountabilityReportMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountabilityReportResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountabilityReportMapper.toDTO(accountabilityReportService.findById(id)));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<PageResponseDTO<AccountabilityReportResponseDTO>> findByContractId(
            @PathVariable UUID contractId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AccountabilityReportResponseDTO> dtoPage = accountabilityReportService.findByContractId(contractId, pageable).map(accountabilityReportMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }
}
