package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.Contract.*;
import com.fiap.hackgov.bidding.internal.services.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService service;

    @PostMapping
    public ResponseEntity<ContractDTO> create(@RequestBody @Valid CreateContractDTO dto) {
        var response = service.create(dto);

        return ResponseEntity
                .created(URI.create("/api/contract/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ContractDTO>> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateContractDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}