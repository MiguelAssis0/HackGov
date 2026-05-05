package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.Supplier.*;
import com.fiap.hackgov.bidding.internal.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService service;

    @PostMapping
    public ResponseEntity<SupplierDTO> create(@RequestBody @Valid CreateSupplierDTO dto) {
        var response = service.create(dto);

        return ResponseEntity
                .created(URI.create("/api/supplier/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SupplierDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateSupplierDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}