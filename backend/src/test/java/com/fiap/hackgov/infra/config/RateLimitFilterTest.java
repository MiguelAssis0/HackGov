package com.fiap.hackgov.infra.config;

import com.fiap.hackgov.infra.filters.RateLimitFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateLimitFilterTest {

    @InjectMocks
    private RateLimitFilter rateLimitFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    @DisplayName("Deve permitir requisição quando dentro do limite")
    void shouldAllowRequestWhenWithinLimit() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("Deve bloquear requisição quando limite excedido")
    void shouldBlockRequestWhenLimitExceeded() throws Exception {
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.2");

        for (int i = 0; i < 20; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response, atLeastOnce()).setStatus(429);
        verify(response, atLeastOnce()).setContentType("application/json");
    }

    @Test
    @DisplayName("Deve usar X-Forwarded-For quando presente")
    void shouldUseXForwardedForHeader() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("Deve usar RemoteAddr quando X-Forwarded-For está ausente")
    void shouldUseRemoteAddrWhenXForwardedForAbsent() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("172.16.0.1");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(request, times(1)).getRemoteAddr();
    }

    @Test
    @DisplayName("IPs diferentes devem ter limites independentes")
    void shouldHaveIndependentLimitsPerIp() throws Exception {
        // Setup local — IP 1 vai bloquear, precisa do writer
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        for (int i = 0; i < 21; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        HttpServletRequest request2 = mock(HttpServletRequest.class);
        HttpServletResponse response2 = mock(HttpServletResponse.class);
        when(request2.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request2.getRemoteAddr()).thenReturn("192.168.1.2");

        rateLimitFilter.doFilterInternal(request2, response2, filterChain);

        verify(response2, never()).setStatus(429);
    }

    @Test
    @DisplayName("Deve retornar JSON no corpo quando bloqueado")
    void shouldReturnJsonBodyWhenBlocked() throws Exception {
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.3");

        for (int i = 0; i < 21; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        assertTrue(responseWriter.toString().contains("Too many requests"));
    }
}