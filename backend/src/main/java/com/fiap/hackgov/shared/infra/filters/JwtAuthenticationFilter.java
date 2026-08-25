package com.fiap.hackgov.shared.infra.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fiap.hackgov.auth.internal.services.UserDetailsServiceImpl;
import com.fiap.hackgov.auth.internal.services.UserSessionService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.shared.infra.exceptions.TokenInvalidException;
import com.fiap.hackgov.shared.infra.services.TokenBlacklistService;
import com.fiap.hackgov.shared.infra.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Order(2)
public class JwtAuthenticationFilter extends BaseSecurityFilter {

    private final TokenService tokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;
    private final FilterErrorWriter filterErrorWriter;
    private final EmployeeRepository employeeRepository;
    private final UserSessionService sessionService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        applyCorsHeaders(request, response);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String token = getToken(request);

        if (token != null) {
            try {
                if (tokenBlacklistService.isBlacklisted(token)) {
                    writeError(response, request, HttpStatus.UNAUTHORIZED, "Unauthorized", "Token has been invalidated");
                    return;
                }

                var userLogin = tokenService.getSubject(token);

                Employee employee = (Employee) userDetailsService.loadUserByUsername(userLogin);
                sessionService.validateAndTouch(tokenService.getSessionId(token), employee);

                var auth = new UsernamePasswordAuthenticationToken(employee, null, employee.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (TokenInvalidException e) {
                writeError(response, request, HttpStatus.UNAUTHORIZED, "Unauthorized", "Token expired");
                return;

            } catch (JWTVerificationException e) {
                writeError(response, request, HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid token");
                return;

            } catch (Exception e) {
                writeError(response, request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Authentication error");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request, HttpStatus status, String title, String detail) throws IOException {
        filterErrorWriter.write(response, request, status, title, detail);
    }

    private String getToken(HttpServletRequest request) {
        var authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.replace("Bearer ", "");
        }
        return null;
    }
}
