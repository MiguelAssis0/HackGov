package com.fiap.hackgov.infra.filters;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.fiap.hackgov.infra.utils.BaseSecurityFilter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@Order(1)
public class RateLimitFilter extends BaseSecurityFilter {

    private static final int DEFAULT_MAX_REQUESTS  = 20;
    private static final int DEFAULT_WINDOW_MINUTES = 1;

    private static final int AUTH_MAX_REQUESTS  = 5;
    private static final int AUTH_WINDOW_MINUTES = 1;

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .maximumSize(100_000)
            .build();

    @Override
    public void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException {

        String clientIp  = getClientIp(request);
        String routeKey  = clientIp + ":" + getRouteKey(request);
        Bucket bucket    = buckets.get(routeKey, key -> newBucket(request));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. Try again later.\"}");
        }
    }

    private Bucket newBucket(HttpServletRequest request) {
        boolean isAuthRoute = isAuthRoute(request);

        int maxRequests   = isAuthRoute ? AUTH_MAX_REQUESTS   : DEFAULT_MAX_REQUESTS;
        int windowMinutes = isAuthRoute ? AUTH_WINDOW_MINUTES : DEFAULT_WINDOW_MINUTES;

        Bandwidth limit = Bandwidth.classic(
                maxRequests,
                Refill.greedy(maxRequests, Duration.ofMinutes(windowMinutes))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private boolean isAuthRoute(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("/api/auth/login") ||
                uri.contains("/api/auth/2fa");
    }

    private String getRouteKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("/api/auth/login")) return "auth:login";
        if (uri.contains("/api/auth/2fa"))   return "auth:2fa";
        return "default";
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "CF-Connecting-IP",   // Cloudflare
                "X-Real-IP",          // Nginx
                "X-Forwarded-For",    // Padrão load balancers
                "Proxy-Client-IP",    // Apache
                "WL-Proxy-Client-IP"  // WebLogic
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For pode ter múltiplos IPs — pega o primeiro (cliente real)
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}