package com.fiap.hackgov.shared.infra.filters;

import com.fiap.hackgov.shared.infra.security.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseSecurityFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:5173",
            "http://82.112.245.100:3000"
    );

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        List<String> skipPaths = securityProperties.getSkipPaths();
        if (skipPaths == null) return false;
        return skipPaths.stream()
                .anyMatch(path -> request.getRequestURI().contains(path));
    }

    protected String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "CF-Connecting-IP",
                "X-Real-IP",
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    protected void applyCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");

        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }

        response.setHeader("Vary", "Origin");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,PATCH,OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", String.join(", ", allowedHeaders(request)));
        response.setHeader("Access-Control-Expose-Headers", "location, Location");
    }

    private Set<String> allowedHeaders(HttpServletRequest request) {
        Set<String> headers = new LinkedHashSet<>();
        headers.add("Authorization");
        headers.add("Content-Type");
        headers.add("Accept");
        headers.add("Origin");
        headers.add("X-Requested-With");

        String requestedHeaders = request.getHeader("Access-Control-Request-Headers");
        if (requestedHeaders != null && !requestedHeaders.isBlank()) {
            for (String value : requestedHeaders.split(",")) {
                String header = value.trim();
                if (!header.isEmpty()) {
                    headers.add(header);
                }
            }
        }

        return headers;
    }
}
