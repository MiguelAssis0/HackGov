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
        if (!MUTATING.contains(request.getMethod()) || response.getStatus() >= 400 || request.getRequestURI().startsWith("/api/auth/")) return;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Employee employee) {
            service.append(employee, request.getMethod(), request.getRequestURI(), response.getStatus(),
                    request.getRemoteAddr(), request.getHeader("User-Agent"));
        }
    }
}
