package com.fiap.hackgov.tasks.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.tasks.internal.DTOs.CrossSectorRequestDTOs.Answer;
import com.fiap.hackgov.tasks.internal.DTOs.CrossSectorRequestDTOs.Create;
import com.fiap.hackgov.tasks.internal.DTOs.CrossSectorRequestDTOs.Response;
import com.fiap.hackgov.tasks.internal.services.CrossSectorTaskRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-requests")
@RequiredArgsConstructor
public class CrossSectorTaskRequestController {
    private final CrossSectorTaskRequestService service;

    @GetMapping
    public List<Response> list(@AuthenticationPrincipal Employee employee) {
        return service.list(employee);
    }

    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody Create dto, @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, employee));
    }

    @PostMapping("/{id}/accept")
    public Response accept(@PathVariable UUID id, @Valid @RequestBody Answer dto, @AuthenticationPrincipal Employee employee) {
        return service.accept(id, dto, employee);
    }

    @PostMapping("/{id}/reject")
    public Response reject(@PathVariable UUID id, @Valid @RequestBody Answer dto, @AuthenticationPrincipal Employee employee) {
        return service.reject(id, dto, employee);
    }
}
