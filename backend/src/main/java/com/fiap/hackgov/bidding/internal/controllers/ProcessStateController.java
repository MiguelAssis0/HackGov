package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.ProcessState.*;
import com.fiap.hackgov.bidding.internal.services.ProcessStateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/process-state")
@RequiredArgsConstructor
public class ProcessStateController {

    private final ProcessStateService service;

    @PostMapping
    public ResponseEntity<ProcessStateResponseDTO> create(
            @RequestBody @Valid CreateProcessStateDTO dto) {

        var response = service.create(dto);

        return ResponseEntity
                .created(URI.create("/api/process-state/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessStateResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProcessStateResponseDTO>> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(service.findAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessStateResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProcessStateDTO dto) {

        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<ProcessStateResponseDTO> finish(@PathVariable UUID id) {
        return ResponseEntity.ok(service.finish(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}