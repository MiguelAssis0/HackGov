package com.fiap.hackgov.documents.internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.ForwardRequest;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.GeneratedRequest;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.Response;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.SignatureRequest;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class MunicipalDocumentController {
    private final MunicipalDocumentService service;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @GetMapping
    public List<Response> list(@RequestParam(required = false) String query,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String number,
                               @RequestParam(required = false) Integer year,
                               @RequestParam(required = false) LocalDate dateStart,
                               @RequestParam(required = false) LocalDate dateEnd,
                               @RequestParam(required = false) String related,
                               @RequestParam(required = false) String tags,
                               @AuthenticationPrincipal Employee employee) {
        return service.list(query, type, number, year, dateStart, dateEnd, related, tags, employee);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> upload(@RequestParam String title,
                                           @RequestParam(defaultValue = "OTHER") String documentType,
                                           @RequestParam(defaultValue = "") String description,
                                           @RequestParam(defaultValue = "PERSONAL") MunicipalDocument.Visibility visibility,
                                           @RequestParam(required = false) String number,
                                           @RequestParam(required = false) Integer year,
                                           @RequestParam(required = false) LocalDate documentDate,
                                           @RequestParam(required = false) String purpose,
                                           @RequestParam(required = false) String keywords,
                                           @RequestParam(required = false) String tags,
                                           @RequestParam(defaultValue = "SEND") MunicipalDocument.Kind kind,
                                           @RequestParam(required = false) Set<UUID> destinationIds,
                                           @RequestPart MultipartFile file,
                                           @AuthenticationPrincipal Employee employee) {
        Response response = service.upload(title, documentType, description, visibility, destinationIds,
                number, year, documentDate, purpose, keywords, tags, kind, file, employee);
        return ResponseEntity.created(URI.create("/api/documents/" + response.id())).body(response);
    }

    @PostMapping(value = "/generated", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> createGenerated(@Valid @RequestBody GeneratedRequest request,
                                                    @AuthenticationPrincipal Employee employee) {
        Response response = service.createGenerated(request, employee);
        return ResponseEntity.created(URI.create("/api/documents/" + response.id())).body(response);
    }

    @PostMapping(value = "/generated", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> createGeneratedWithAttachment(@RequestPart("payload") String payload,
                                                                    @RequestPart(value = "file", required = false) MultipartFile file,
                                                                    @AuthenticationPrincipal Employee employee) {
        try {
            GeneratedRequest request = OBJECT_MAPPER.readValue(payload, GeneratedRequest.class);
            Response response = service.createGenerated(request, employee, file);
            return ResponseEntity.created(URI.create("/api/documents/" + response.id())).body(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Dados do documento invalidos", exception);
        }
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
    public Response signHomologation(@PathVariable UUID id, @Valid @RequestBody SignatureRequest request,
                                     @AuthenticationPrincipal Employee employee) {
        return service.signHomologation(id, request, employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        service.delete(id, employee);
        return ResponseEntity.noContent().build();
    }
}
