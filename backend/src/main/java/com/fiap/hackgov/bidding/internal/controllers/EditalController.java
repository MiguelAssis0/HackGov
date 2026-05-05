package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.Edital.*;
import com.fiap.hackgov.bidding.internal.services.EditalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/edital")
@RequiredArgsConstructor
public class EditalController {

    private final EditalService service;

    @PostMapping
    public ResponseEntity<EditalDTO> create(@RequestBody @Valid CreateEditalDTO dto) {
        var response = service.create(dto);

        return ResponseEntity
                .created(URI.create("/api/edital/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EditalDTO>> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EditalDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EditalDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateEditalDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}