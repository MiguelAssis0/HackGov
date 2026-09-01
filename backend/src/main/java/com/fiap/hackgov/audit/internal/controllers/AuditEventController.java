package com.fiap.hackgov.audit.internal.controllers;

import com.fiap.hackgov.audit.internal.DTOs.AuditDtos;
import com.fiap.hackgov.audit.internal.services.AuditEventService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditEventController {
    private final AuditEventService service;

    @GetMapping
    public AuditDtos.Page list(@RequestParam(defaultValue = "prefeitura") String scope,
                               @RequestParam(required = false) String prefeitura,
                               @RequestParam(defaultValue = "") String q,
                               @RequestParam(defaultValue = "todos") String tipo,
                               @RequestParam(defaultValue = "") String modulo,
                               @RequestParam(defaultValue = "") String acao,
                               @RequestParam(defaultValue = "") String usuario,
                               @RequestParam(defaultValue = "") String dataInicial,
                               @RequestParam(defaultValue = "") String dataFinal,
                               @RequestParam(defaultValue = "0") int page,
                               @AuthenticationPrincipal Employee employee) {
        return service.list(scope, prefeitura, q, tipo, modulo, acao, usuario, dataInicial, dataFinal, page, employee);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "prefeitura") String scope,
                                         @RequestParam(required = false) String prefeitura,
                                         @RequestParam(defaultValue = "") String q,
                                         @RequestParam(defaultValue = "todos") String tipo,
                                         @RequestParam(defaultValue = "") String modulo,
                                         @RequestParam(defaultValue = "") String acao,
                                         @RequestParam(defaultValue = "") String usuario,
                                         @RequestParam(defaultValue = "") String dataInicial,
                                         @RequestParam(defaultValue = "") String dataFinal,
                                         @AuthenticationPrincipal Employee employee) {
        byte[] csv = service.exportCsv(scope, prefeitura, q, tipo, modulo, acao, usuario, dataInicial, dataFinal, employee)
                .getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditoria.csv").body(csv);
    }

    @GetMapping("/verify")
    public Verification verify(@AuthenticationPrincipal Employee employee) {
        return new Verification(service.verify(employee));
    }

    public record Verification(boolean valid) {
    }
}
