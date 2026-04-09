package com.fiap.hackgov.shared.infra.filters;

import com.fiap.hackgov.shared.infra.security.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

public abstract class BaseSecurityFilter extends OncePerRequestFilter {

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
}