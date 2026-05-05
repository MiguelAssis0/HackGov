package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.Effort.*;
import com.fiap.hackgov.bidding.internal.services.EffortService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/effort")
@RequiredArgsConstructor
public class EffortController {

    private final EffortService service;

    @PostMapping
    public ResponseEntity<EffortDTO> create(@RequestBody @Valid CreateEffortDTO dto) {
        var response = service.create(dto);

        return ResponseEntity
                .created(URI.create("/api/effort/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EffortDTO>> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EffortDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EffortDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateEffortDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}