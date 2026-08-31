package com.fiap.hackgov.imports.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.imports.internal.DTOs.ImportDTOs.BatchResponse;
import com.fiap.hackgov.imports.internal.DTOs.ImportDTOs.Preview;
import com.fiap.hackgov.imports.internal.DTOs.ImportDTOs.ValidateRequest;
import com.fiap.hackgov.imports.internal.DTOs.ImportDTOs.ValidationReport;
import com.fiap.hackgov.imports.internal.services.SpreadsheetImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class SpreadsheetImportController {
    private final SpreadsheetImportService service;

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Preview preview(@RequestParam String target, @RequestPart("file") MultipartFile file, @AuthenticationPrincipal Employee e) {
        return service.preview(target, file, e);
    }

    @PostMapping("/{id}/validate")
    public ValidationReport validate(@PathVariable UUID id, @Valid @RequestBody ValidateRequest r, @AuthenticationPrincipal Employee e) {
        return service.validate(id, r, e);
    }

    @PostMapping("/{id}/execute")
    public BatchResponse execute(@PathVariable UUID id, @AuthenticationPrincipal Employee e) {
        return service.execute(id, e);
    }

    @GetMapping("/history")
    public List<BatchResponse> history(@AuthenticationPrincipal Employee e) {
        return service.history(e);
    }
}
