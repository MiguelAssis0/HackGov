package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.Approval.ApprovalResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Approval.CreateApprovalDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Approval.UpdateApprovalDTO;
import com.fiap.hackgov.bidding.internal.services.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService service;

    @PostMapping
    public ResponseEntity<ApprovalResponseDTO> create(@RequestBody @Valid CreateApprovalDTO dto) {
        var response = service.create(dto);

        return ResponseEntity
                .created(URI.create("/api/approval/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ApprovalResponseDTO>> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApprovalResponseDTO> processApproval(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateApprovalDTO dto
    ) {
        return ResponseEntity.ok(service.processApproval(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}