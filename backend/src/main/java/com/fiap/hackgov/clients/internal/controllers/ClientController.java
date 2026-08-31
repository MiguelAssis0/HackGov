package com.fiap.hackgov.clients.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.clients.internal.DTOs.ClientDTOs.Response;
import com.fiap.hackgov.clients.internal.DTOs.ClientDTOs.SaveRequest;
import com.fiap.hackgov.clients.internal.DTOs.ClientDTOs.ServiceRequest;
import com.fiap.hackgov.clients.internal.DTOs.ClientDTOs.ServiceResponse;
import com.fiap.hackgov.clients.internal.services.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService service;

    @GetMapping
    public Page<Response> findAll(@RequestParam(defaultValue = "") String query, Pageable pageable, @AuthenticationPrincipal Employee employee) {
        return service.findAll(query, pageable, employee);
    }

    @GetMapping("/{id}")
    public Response findById(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        return service.findById(id, employee);
    }

    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody SaveRequest request, @AuthenticationPrincipal Employee employee) {
        Response response = service.create(request, employee);
        return ResponseEntity.created(URI.create("/api/clients/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public Response update(@PathVariable UUID id, @Valid @RequestBody SaveRequest request, @AuthenticationPrincipal Employee employee) {
        return service.update(id, request, employee);
    }

    @PostMapping("/{id}/services")
    public ServiceResponse addService(@PathVariable UUID id, @Valid @RequestBody ServiceRequest request, @AuthenticationPrincipal Employee employee) {
        return service.addService(id, request, employee);
    }
}
