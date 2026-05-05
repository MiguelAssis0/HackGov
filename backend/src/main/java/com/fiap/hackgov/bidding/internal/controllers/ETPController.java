package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.ETP.*;
import com.fiap.hackgov.bidding.internal.services.ETPService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/etp")
@RequiredArgsConstructor
public class ETPController {

    private final ETPService service;

    @PostMapping
    public ResponseEntity<ETPDTO> create(@RequestBody @Valid CreateETPDTO dto) {
        var response = service.create(dto);

        return ResponseEntity
                .created(URI.create("/api/etp/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ETPDTO>> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ETPDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ETPDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateETPDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}