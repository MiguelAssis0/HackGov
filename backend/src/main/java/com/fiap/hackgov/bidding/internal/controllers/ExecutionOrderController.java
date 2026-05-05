package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.ExecutionOrder.*;
import com.fiap.hackgov.bidding.internal.services.ExecutionOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/execution-order")
@RequiredArgsConstructor
public class ExecutionOrderController {

    private final ExecutionOrderService service;

    @PostMapping
    public ResponseEntity<ExecutionOrderDTO> create(@RequestBody @Valid CreateExecutionOrderDTO dto) {
        var response = service.create(dto);

        return ResponseEntity
                .created(URI.create("/api/execution-order/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ExecutionOrderDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionOrderDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExecutionOrderDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateExecutionOrderDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}