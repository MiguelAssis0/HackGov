package com.fiap.hackgov.infra.config;

import com.fiap.hackgov.infra.filters.RateLimitFilter;
import com.fiap.hackgov.infra.utils.AuditLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private AuditLog auditLog;

    @Mock
    private AuditLog.Builder builderMock;

    @BeforeEach
    void setUp() {
        lenient().when(auditLog.with(any())).thenReturn(builderMock);
        lenient().when(builderMock.event(any())).thenReturn(builderMock);
        lenient().when(builderMock.email(any())).thenReturn(builderMock);
        lenient().when(builderMock.reason(any())).thenReturn(builderMock);
        lenient().when(builderMock.level(any())).thenReturn(builderMock);
    }

    // ==================== Limite padrão (20 req/min) ====================

    @Test
    @DisplayName("Deve permitir requisição padrão dentro do limite")
    void shouldAllowDefaultRequestWhenWithinLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("Deve bloquear rota padrão após 20 requisições")
    void shouldBlockDefaultRouteAfterTwentyRequests() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.2");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        for (int i = 0; i < 21; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response, atLeastOnce()).setStatus(429);
        verify(response, atLeastOnce()).setContentType("application/json");
    }

    // ==================== Limite auth (5 req/min) ====================

    @Test
    @DisplayName("Deve bloquear rota de login após 5 requisições")
    void shouldBlockLoginRouteAfterFiveRequests() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.3");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response, atLeastOnce()).setStatus(429);
    }

    @Test
    @DisplayName("Deve bloquear rota de 2FA após 5 requisições")
    void shouldBlockTwoFactorRouteAfterFiveRequests() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/2fa/verify");
        when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.4");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        for (int i = 0; i < 5; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response, atLeastOnce()).setStatus(429);
    }

    @Test
    @DisplayName("Login e rota padrão devem ter limites independentes por rota")
    void shouldHaveIndependentLimitsPerRoute() throws Exception {
        String ip = "192.168.0.5";
        when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(ip);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // Esgota o limite do login (5)
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        for (int i = 0; i < 6; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        // Rota padrão com mesmo IP deve ainda funcionar
        HttpServletRequest defaultRequest = mock(HttpServletRequest.class);
        HttpServletResponse defaultResponse = mock(HttpServletResponse.class);
        when(defaultRequest.getRequestURI()).thenReturn("/api/test");
        when(defaultRequest.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(defaultRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(defaultRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(defaultRequest.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(defaultRequest.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(defaultRequest.getRemoteAddr()).thenReturn(ip);

        rateLimitFilter.doFilterInternal(defaultRequest, defaultResponse, filterChain);

        verify(defaultResponse, never()).setStatus(429);
    }

    // ==================== Headers de proxy ====================

    @Test
    @DisplayName("Deve priorizar CF-Connecting-IP sobre outros headers")
    void shouldPrioritizeCfConnectingIp() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("CF-Connecting-IP")).thenReturn("1.1.1.1");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRemoteAddr();
    }

    @Test
    @DisplayName("Deve usar X-Real-IP quando CF-Connecting-IP está ausente")
    void shouldUseXRealIpWhenCfAbsent() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("2.2.2.2");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRemoteAddr();
    }

    @Test
    @DisplayName("Deve usar X-Forwarded-For quando headers anteriores estão ausentes")
    void shouldUseXForwardedForWhenOthersAbsent() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRemoteAddr();
    }

    @Test
    @DisplayName("Deve usar RemoteAddr quando nenhum header de proxy está presente")
    void shouldUseRemoteAddrWhenNoProxyHeaders() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("172.16.0.1");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request).getRemoteAddr();
    }

    // ==================== IPs independentes ====================

    @Test
    @DisplayName("IPs diferentes devem ter limites independentes")
    void shouldHaveIndependentLimitsPerIp() throws Exception {
        // IP 1 — esgota o limite
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        for (int i = 0; i < 21; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        // IP 2 — deve passar normalmente
        HttpServletRequest request2 = mock(HttpServletRequest.class);
        HttpServletResponse response2 = mock(HttpServletResponse.class);
        when(request2.getRequestURI()).thenReturn("/api/test");
        when(request2.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request2.getHeader("X-Real-IP")).thenReturn(null);
        when(request2.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request2.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request2.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request2.getRemoteAddr()).thenReturn("192.168.1.2");

        rateLimitFilter.doFilterInternal(request2, response2, filterChain);

        verify(response2, never()).setStatus(429);
    }

    // ==================== Corpo da resposta ====================

    @Test
    @DisplayName("Deve retornar JSON no corpo quando bloqueado")
    void shouldReturnJsonBodyWhenBlocked() throws Exception {
        StringWriter responseWriter = new StringWriter();
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.6");
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        for (int i = 0; i < 21; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        assertTrue(responseWriter.toString().contains("Too many requests"));
    }
}