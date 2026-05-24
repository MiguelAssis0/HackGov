package com.fiap.hackgov.shared.infra.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.hackgov.auth.internal.services.UserDetailsServiceImpl;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.shared.infra.exceptions.TokenInvalidException;
import com.fiap.hackgov.shared.infra.exceptions.controllers.StandardError;
import com.fiap.hackgov.shared.infra.services.TokenBlacklistService;
import com.fiap.hackgov.shared.infra.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Order(2)
public class JwtAuthenticationFilter extends BaseSecurityFilter {

    private final TokenService tokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        applyCorsHeaders(request, response);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String token = getToken(request);

        try {
            if (token != null) {
                if (tokenBlacklistService.isBlacklisted(token)) {
                    writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Token has been invalidated");
                    return;
                }

                var userLogin = tokenService.getSubject(token);

                Employee employee = (Employee) userDetailsService.loadUserByUsername(userLogin);

                var auth = new UsernamePasswordAuthenticationToken(employee, null, employee.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);

        } catch (TokenInvalidException e) {
            writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Token expired");

        } catch (JWTVerificationException e) {
            writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Invalid token");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            writeError(response, request, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", "Authentication error");
        }
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request,
                            int status, String error, String message) throws IOException {
        applyCorsHeaders(request, response);

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

    private String getToken(HttpServletRequest request) {
        var authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.replace("Bearer ", "");
        }
        return null;
    }
}
