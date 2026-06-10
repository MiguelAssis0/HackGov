package com.fiap.hackgov.shared.infra.filters;

import com.fiap.hackgov.shared.infra.utils.AuditLog;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@Order(1)
public class RateLimitFilter extends BaseSecurityFilter {

    private static final int DEFAULT_MAX_REQUESTS = 20;
    private static final int DEFAULT_WINDOW_MINUTES = 1;

    private static final int AUTH_MAX_REQUESTS = 5;
    private static final int AUTH_WINDOW_MINUTES = 1;

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .maximumSize(100_000)
            .build();

    @Autowired
    private AuditLog auditLog;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) throws ServletException, IOException {
        applyCorsHeaders(request, response);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (!isAuthRoute(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String routeKey = clientIp + ":" + getRouteKey(request);
        Bucket bucket = buckets.get(routeKey, key -> newBucket(request));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            auditLog.with(log).event("rate_limit_exceeded").reason("Too many requests").level(AuditLog.Level.WARN).log();
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. Try again later.\"}");
        }
    }

    private Bucket newBucket(HttpServletRequest request) {
        int maxRequests = AUTH_MAX_REQUESTS;
        int windowMinutes = AUTH_WINDOW_MINUTES;
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(maxRequests)
                        .refillIntervally(maxRequests, Duration.ofMinutes(windowMinutes)))
                .build();
    }

    private boolean isAuthRoute(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("/api/auth/login") ||
                uri.contains("/api/auth/2fa");
    }

    private String getRouteKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("/api/auth/login")) return "auth:login";
        if (uri.contains("/api/auth/2fa")) return "auth:2fa";
        return "auth";
    }
}
