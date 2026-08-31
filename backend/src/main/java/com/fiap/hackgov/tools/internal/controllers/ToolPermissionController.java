package com.fiap.hackgov.tools.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.tools.internal.services.ToolPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tool-permissions")
@RequiredArgsConstructor
public class ToolPermissionController {
    private final ToolPermissionService service;

    @GetMapping
    public List<ToolPermissionService.Response> list(@AuthenticationPrincipal Employee e) {
        return service.list(e);
    }

    @PostMapping
    public ResponseEntity<ToolPermissionService.Response> create(@RequestBody ToolPermissionService.Create r, @AuthenticationPrincipal Employee e) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r, e));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Employee e) {
        service.delete(id, e);
        return ResponseEntity.noContent().build();
    }
}
