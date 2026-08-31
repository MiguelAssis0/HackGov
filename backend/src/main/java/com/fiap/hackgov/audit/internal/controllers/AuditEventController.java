package com.fiap.hackgov.audit.internal.controllers;

import com.fiap.hackgov.audit.internal.entities.AuditEvent;
import com.fiap.hackgov.audit.internal.services.AuditEventService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditEventController {
    private final AuditEventService service;

    @GetMapping
    public List<AuditEvent> list(@RequestParam(required = false) String query,
                                 @AuthenticationPrincipal Employee employee) {
        return service.list(query, employee);
    }

    @GetMapping("/verify")
    public Verification verify(@AuthenticationPrincipal Employee employee) {
        return new Verification(service.verify(employee));
    }

    public record Verification(boolean valid) {
    }
}
