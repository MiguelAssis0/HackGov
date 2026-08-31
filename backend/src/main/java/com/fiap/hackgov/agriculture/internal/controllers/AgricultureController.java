package com.fiap.hackgov.agriculture.internal.controllers;

import com.fiap.hackgov.agriculture.internal.DTOs.AgricultureDTOs.*;
import com.fiap.hackgov.agriculture.internal.services.AgricultureService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/agriculture")
@RequiredArgsConstructor
public class AgricultureController {
    private final AgricultureService service;

    @GetMapping("/catalog")
    public CatalogResponse catalog(@AuthenticationPrincipal Employee employee) {
        return service.catalog(employee);
    }

    @PostMapping("/catalog/{kind}")
    public CatalogItem addCatalog(@PathVariable String kind, @Valid @RequestBody CatalogRequest request, @AuthenticationPrincipal Employee employee) {
        return service.addCatalog(kind, request, employee);
    }

    @GetMapping("/services")
    public Page<ServiceResponse> findAll(@RequestParam(defaultValue = "") String query, Pageable pageable, @AuthenticationPrincipal Employee employee) {
        return service.findAll(query, pageable, employee);
    }

    @PostMapping("/services")
    public ServiceResponse create(@Valid @RequestBody ServiceRequest request, @AuthenticationPrincipal Employee employee) {
        return service.create(request, employee);
    }

    @PutMapping("/services/{id}")
    public ServiceResponse update(@PathVariable UUID id, @Valid @RequestBody ServiceRequest request, @AuthenticationPrincipal Employee employee) {
        return service.update(id, request, employee);
    }

    @PutMapping("/services/{id}/control")
    public ControlResponse control(@PathVariable UUID id, @Valid @RequestBody ControlRequest request, @AuthenticationPrincipal Employee employee) {
        return service.updateControl(id, request, employee);
    }

    @PostMapping(value = "/services/{id}/proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse proof(@PathVariable UUID id, @RequestPart("file") MultipartFile file, @AuthenticationPrincipal Employee employee) {
        return service.uploadProof(id, file, employee);
    }
}
