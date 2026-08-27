package com.fiap.hackgov.tools.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.tools.internal.services.ToolCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tool-categories")
@RequiredArgsConstructor
public class ToolCategoryController {
    private final ToolCategoryService service;

    @GetMapping
    public List<ToolCategoryService.Response> list(@AuthenticationPrincipal Employee employee) {
        return service.list(employee);
    }

    @PostMapping
    public ResponseEntity<ToolCategoryService.Response> create(@RequestBody ToolCategoryService.Request request,
                                                                @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, employee));
    }

    @PutMapping("/{id}")
    public ToolCategoryService.Response update(@PathVariable UUID id,
                                                @RequestBody ToolCategoryService.Request request,
                                                @AuthenticationPrincipal Employee employee) {
        return service.update(id, request, employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        service.delete(id, employee);
        return ResponseEntity.noContent().build();
    }
}
