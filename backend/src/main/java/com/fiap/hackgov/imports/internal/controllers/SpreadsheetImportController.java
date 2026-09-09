package com.fiap.hackgov.imports.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.imports.internal.DTOs.ImportDTOs.BatchResponse;
import com.fiap.hackgov.imports.internal.DTOs.ImportDTOs.Preview;
import com.fiap.hackgov.imports.internal.DTOs.ImportDTOs.ValidateRequest;
import com.fiap.hackgov.imports.internal.DTOs.ImportDTOs.ValidationReport;
import com.fiap.hackgov.imports.internal.services.SpreadsheetImportService;
import com.fiap.hackgov.imports.internal.services.SpreadsheetImportService.DownloadFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/templates/{target}")
    public ResponseEntity<byte[]> template(@PathVariable String target, @AuthenticationPrincipal Employee e) {
        return file(service.template(target, e), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/exports/{target}")
    public ResponseEntity<byte[]> export(@PathVariable String target, @AuthenticationPrincipal Employee e) {
        return file(service.export(target, e), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id, @AuthenticationPrincipal Employee e) {
        return file(service.original(id, e), MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    private ResponseEntity<byte[]> file(DownloadFile file, String contentType) {
        ContentDisposition disposition = ContentDisposition.attachment().filename(file.filename()).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).contentLength(file.content().length).header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString()).body(file.content());
    }
}
