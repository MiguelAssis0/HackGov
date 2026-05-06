package com.fiap.hackgov.shared.infra.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.hackgov.auth.internal.services.UserDetailsServiceImpl;
import com.fiap.hackgov.shared.infra.exceptions.TokenInvalidException;
import com.fiap.hackgov.shared.infra.exceptions.controllers.StandardError;
import com.fiap.hackgov.shared.infra.services.TokenBlacklistService;
import com.fiap.hackgov.shared.infra.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@Order(2)
public class JwtAuthenticationFilter extends BaseSecurityFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = getToken(request);

        try {
            if (token != null) {
                if (tokenBlacklistService.isBlacklisted(token)) {
                    writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Token has been invalidated");
                    return;
                }

                var userLogin = tokenService.getSubject(token);

                UserDetails user = userDetailsService.loadUserByUsername(userLogin);
                var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
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