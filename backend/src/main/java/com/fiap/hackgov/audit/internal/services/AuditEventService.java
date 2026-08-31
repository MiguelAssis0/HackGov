package com.fiap.hackgov.audit.internal.services;

import com.fiap.hackgov.audit.internal.entities.AuditEvent;
import com.fiap.hackgov.audit.internal.repositories.AuditEventRepository;
import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditEventService {
    private static final String GENESIS = "0".repeat(64);
    private final AuditEventRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized void append(Employee employee, String method, String path, int status,
                                    String remoteAddress, String userAgent) {
        if (employee == null || employee.getCityHallId() == null) return;
        UUID cityId = employee.getCityHallId().getId();
        String previous = repository.findTopByCityHallIdOrderByIdDesc(cityId)
                .map(AuditEvent::getEventHash).orElse(GENESIS);
        LocalDateTime occurredAt = LocalDateTime.now().withNano(0);
        AuditEvent event = new AuditEvent();
        event.setCityHallId(cityId);
        event.setActorId(employee.getId());
        event.setActorEmail(employee.getEmail());
        event.setMethod(method);
        event.setPath(path);
        event.setResponseStatus(status);
        event.setRemoteAddress(trim(remoteAddress, 80));
        event.setUserAgent(trim(userAgent, 400));
        event.setPreviousHash(previous);
        event.setCreatedAt(occurredAt);
        event.setEventHash(sha256(canonical(event)));
        repository.save(event);
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> list(String query, Employee employee) {
        requireAdmin(employee);
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return repository.findTop500ByCityHallIdOrderByIdDesc(employee.getCityHallId().getId()).stream()
                .filter(event -> q.isBlank() || event.getActorEmail().toLowerCase(Locale.ROOT).contains(q)
                        || event.getPath().toLowerCase(Locale.ROOT).contains(q)
                        || event.getMethod().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean verify(Employee employee) {
        requireAdmin(employee);
        List<AuditEvent> events = repository.findTop500ByCityHallIdOrderByIdDesc(employee.getCityHallId().getId());
        for (int index = 0; index < events.size(); index++) {
            AuditEvent event = events.get(index);
            if (!event.getEventHash().equals(sha256(canonical(event)))) return false;
            if (index < events.size() - 1 && !event.getPreviousHash().equals(events.get(index + 1).getEventHash()))
                return false;
        }
        return true;
    }

    private void requireAdmin(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado");
        if (employee.getCityHallId() == null)
            throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        if (!Roles.ADMIN.equals(employee.getRole()))
            throw new BusinessException("Somente administradores podem consultar a auditoria");
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 indisponivel", exception);
        }
    }

    private String canonical(AuditEvent event) {
        return String.join("|", event.getPreviousHash(), event.getCityHallId().toString(), event.getActorId().toString(),
                event.getMethod(), event.getPath(), Integer.toString(event.getResponseStatus()), event.getCreatedAt().toString());
    }

    private String trim(String value, int limit) {
        if (value == null) return "";
        return value.length() <= limit ? value : value.substring(0, limit);
    }
}
