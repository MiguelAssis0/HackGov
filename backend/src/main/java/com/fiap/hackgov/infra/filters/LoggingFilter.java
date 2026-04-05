package com.fiap.hackgov.infra.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            MDC.put("requestId", UUID.randomUUID().toString());
            MDC.put("ip",        request.getRemoteAddr());
            MDC.put("path",      request.getRequestURI());
            MDC.put("method",    request.getMethod());

            filterChain.doFilter(request, response);

        } finally {
            MDC.put("status",        String.valueOf(response.getStatus()));
            MDC.put("durationMs",    String.valueOf(System.currentTimeMillis() - startTime));
            log.info("event=request_completed");
            MDC.clear();
        }
    }
}