package com.fiap.hackgov.audit.internal.config;

import com.fiap.hackgov.audit.internal.services.AuditEventService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {
    private static final Set<String> MUTATING = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final AuditEventService service;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        // ponytail: login/logout via AuthService (sem principal aqui); auditoria nunca pode quebrar a resposta
        try {
            String uri = request.getRequestURI();
            if (uri.startsWith("/api/auth/")) return;
            String method = request.getMethod();
            String toolSlug = AuditEventService.toolSlugFor(uri);
            if (!MUTATING.contains(method) && toolSlug == null) return;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Employee employee) {
                String risk = service.riskFor(employee, toolSlug, method, response.getStatus());
                service.append(employee, method, uri, response.getStatus(),
                        request.getRemoteAddr(), request.getHeader("User-Agent"), risk);
            }
        } catch (Exception ignored) {
        }
    }
}
