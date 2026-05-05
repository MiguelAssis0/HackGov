package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.Bidding.*;
import com.fiap.hackgov.bidding.internal.services.BiddingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/bidding")
@RequiredArgsConstructor
public class BiddingController {

    private final BiddingService service;

    @PostMapping
    public ResponseEntity<BiddingProcessDTO> create(@RequestBody @Valid CreateBiddingProcessDTO dto) {
        var response = service.create(dto);

        return ResponseEntity
                .created(URI.create("/api/bidding/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<BiddingProcessDTO>> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BiddingProcessDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BiddingProcessDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateBiddingProcessDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}