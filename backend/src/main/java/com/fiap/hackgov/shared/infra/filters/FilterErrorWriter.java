package com.fiap.hackgov.shared.infra.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.hackgov.shared.infra.exceptions.controllers.StandardError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class FilterErrorWriter {

    private final ObjectMapper objectMapper;

    public FilterErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, HttpServletRequest request,
                      int status, String error, String message) throws IOException {

        StandardError standardError = new StandardError(
                Instant.now(),
                status,
                error,
                message,
                request.getRequestURI()
        );

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(standardError));
    }
}
