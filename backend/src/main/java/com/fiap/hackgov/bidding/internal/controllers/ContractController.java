package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.contract.ContractResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.contract.CreateContractDTO;
import com.fiap.hackgov.bidding.internal.entities.Contract;
import com.fiap.hackgov.bidding.internal.mappers.ContractMapper;
import com.fiap.hackgov.bidding.internal.services.ContractService;
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
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final ContractMapper contractMapper;
    private final PaginationMapper paginationMapper;

    @PostMapping
    public ResponseEntity<ContractResponseDTO> create(@Valid @RequestBody CreateContractDTO dto) {

        Contract contract = contractService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(contractMapper.toDTO(contract));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<ContractResponseDTO>> findAll(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ContractResponseDTO> dtoPage = contractService.findAll(pageable).map(contractMapper::toDTO);

        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(contractMapper.toDTO(contractService.findById(id)));
    }

    @GetMapping("/licitation-process/{licitationProcessId}")
    public ResponseEntity<ContractResponseDTO> findByLicitationProcessId(@PathVariable UUID licitationProcessId) {
        return ResponseEntity.ok(contractMapper.toDTO(contractService.findByLicitationProcessId(licitationProcessId)));
    }
}
