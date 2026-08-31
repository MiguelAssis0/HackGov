package com.fiap.hackgov.documents.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.ForwardRequest;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.GeneratedRequest;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.Response;
import com.fiap.hackgov.documents.internal.entities.MunicipalDocument;
import com.fiap.hackgov.documents.internal.services.MunicipalDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class MunicipalDocumentController {
    private final MunicipalDocumentService service;

    @GetMapping
    public List<Response> list(@RequestParam(required = false) String query,
                               @RequestParam(required = false) String type,
                               @AuthenticationPrincipal Employee employee) {
        return service.list(query, type, employee);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> upload(@RequestParam String title,
                                           @RequestParam(defaultValue = "OTHER") String documentType,
                                           @RequestParam(defaultValue = "") String description,
                                           @RequestParam(defaultValue = "PERSONAL") MunicipalDocument.Visibility visibility,
                                           @RequestParam(required = false) Set<UUID> destinationIds,
                                           @RequestPart MultipartFile file,
                                           @AuthenticationPrincipal Employee employee) {
        Response response = service.upload(title, documentType, description, visibility, destinationIds, file, employee);
        return ResponseEntity.created(URI.create("/api/documents/" + response.id())).body(response);
    }

    @PostMapping("/generated")
    public ResponseEntity<Response> createGenerated(@Valid @RequestBody GeneratedRequest request,
                                                    @AuthenticationPrincipal Employee employee) {
        Response response = service.createGenerated(request, employee);
        return ResponseEntity.created(URI.create("/api/documents/" + response.id())).body(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        MunicipalDocument document = service.download(id, employee);
        ContentDisposition disposition = ContentDisposition.attachment().filename(document.getOriginalName()).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(document.getContentType()))
                .contentLength(document.getSizeBytes()).header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(document.getContent());
    }

    @PostMapping("/{id}/forward")
    public Response forward(@PathVariable UUID id, @Valid @RequestBody ForwardRequest request,
                            @AuthenticationPrincipal Employee employee) {
        return service.forward(id, request, employee);
    }

    @PostMapping("/{id}/sign-homologation")
    public Response signHomologation(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        return service.signHomologation(id, employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        service.delete(id, employee);
        return ResponseEntity.noContent().build();
    }
}
