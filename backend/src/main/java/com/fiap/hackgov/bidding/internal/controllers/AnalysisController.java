package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.analysis.AnalysisResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.analysis.CreateAnalysisDTO;
import com.fiap.hackgov.bidding.internal.DTOs.analysis.UpdateAnalysisDTO;
import com.fiap.hackgov.bidding.internal.services.AnalysisService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<AnalysisResponseDTO> create(@RequestBody @Valid CreateAnalysisDTO dto) {
        AnalysisResponseDTO response = analysisService.create(dto);

        return ResponseEntity.created(URI.create("/api/analyses/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<AnalysisResponseDTO>> findAll(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(analysisService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(analysisService.findById(id));
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<AnalysisResponseDTO>> findPending(Pageable pageable) {
        return ResponseEntity.ok(analysisService.findPending(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AnalysisResponseDTO> processAnalysis(@AuthenticationPrincipal Employee employee, @PathVariable UUID id, @RequestBody @Valid UpdateAnalysisDTO dto) {
        return ResponseEntity.ok(analysisService.processAnalysis(id, dto, employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        analysisService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
