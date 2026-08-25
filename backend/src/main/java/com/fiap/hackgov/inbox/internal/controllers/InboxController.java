package com.fiap.hackgov.inbox.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.inbox.internal.DTOs.InboxDTOs.Response;
import com.fiap.hackgov.inbox.internal.entities.InboxEntry;
import com.fiap.hackgov.inbox.internal.services.InboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inbox")
@RequiredArgsConstructor
public class InboxController {
    private final InboxService service;

    @GetMapping
    public Page<Response> findAll(
            @RequestParam(required = false) InboxEntry.Status status,
            @RequestParam(required = false) InboxEntry.Type type,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "") String query,
            Pageable pageable,
            @AuthenticationPrincipal Employee employee
    ) {
        return service.findVisible(status, type, unreadOnly, query, pageable, employee);
    }

    @PatchMapping("/{id}/read")
    public Response read(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        return service.read(id, employee);
    }

    @PatchMapping("/{id}/claim")
    public Response claim(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        return service.claim(id, employee);
    }

    @PatchMapping("/{id}/complete")
    public Response complete(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        return service.complete(id, employee);
    }
}
