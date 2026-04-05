package com.fiap.hackgov.infra.utils;

import com.fiap.hackgov.infra.security.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

public abstract class BaseSecurityFilter extends OncePerRequestFilter {

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        List<String> skipPaths = securityProperties.getSkipPaths();
        if (skipPaths == null) return false;
        return skipPaths.stream()
                .anyMatch(path -> request.getRequestURI().contains(path));
    }
}