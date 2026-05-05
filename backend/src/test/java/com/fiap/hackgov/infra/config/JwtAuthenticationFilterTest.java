package com.fiap.hackgov.infra.config;

import com.fiap.hackgov.shared.infra.filters.JwtAuthenticationFilter;
import com.fiap.hackgov.shared.infra.security.SecurityProperties;
import com.fiap.hackgov.shared.infra.services.TokenService;
import com.fiap.hackgov.shared.infra.services.TokenBlacklistService;
import com.fiap.hackgov.auth.internal.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private static final String TOKEN = "valid.jwt.token";
    private static final String EMAIL = "user@test.com";
    private static final String BEARER_TOKEN = "Bearer " + TOKEN;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "tokenService", tokenService);
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "tokenBlacklistService", tokenBlacklistService);
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "securityProperties", securityProperties);
        SecurityContextHolder.clearContext();
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== Sem token ====================

    @Test
    @DisplayName("Deve passar sem autenticar quando Authorization header está ausente")
    void shouldPassWithoutAuthenticatingWhenNoAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenBlacklistService, never()).isBlacklisted(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Deve passar sem autenticar quando Authorization não começa com Bearer")
    void shouldPassWithoutAuthenticatingWhenNotBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenBlacklistService, never()).isBlacklisted(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ==================== Token blacklistado ====================

    @Test
    @DisplayName("Deve retornar 401 quando token está blacklistado")
    void shouldReturn401WhenTokenIsBlacklisted() throws Exception {
        StringWriter responseWriter = new StringWriter();
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isBlacklisted(TOKEN)).thenReturn(true);
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        assertTrue(responseWriter.toString().contains("Token has been invalidated"));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Não deve chamar tokenService quando token está blacklistado")
    void shouldNotCallTokenServiceWhenBlacklisted() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isBlacklisted(TOKEN)).thenReturn(true);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(tokenService, never()).getSubject(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    // ==================== Token válido ====================

    @Test
    @DisplayName("Deve autenticar usuário quando token é válido")
    void shouldAuthenticateUserWhenTokenIsValid() throws Exception {
        UserDetails userDetails = new User(EMAIL, "password",
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isBlacklisted(TOKEN)).thenReturn(false);
        when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(EMAIL, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    @DisplayName("Deve setar autenticação no SecurityContext com authorities corretas")
    void shouldSetAuthenticationWithCorrectAuthorities() throws Exception {
        UserDetails userDetails = new User(EMAIL, "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isBlacklisted(TOKEN)).thenReturn(false);
        when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Deve extrair token corretamente removendo o prefixo Bearer")
    void shouldExtractTokenRemovingBearerPrefix() throws Exception {
        UserDetails userDetails = new User(EMAIL, "password", List.of());

        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isBlacklisted(TOKEN)).thenReturn(false);
        when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Verifica que o token sem "Bearer " foi passado para o tokenService
        verify(tokenService).getSubject(TOKEN);
        verify(tokenBlacklistService).isBlacklisted(TOKEN);
    }

    // ==================== Token inválido ====================

    @Test
    @DisplayName("Deve delegar exceção para HandlerExceptionResolver quando token é inválido")
    void shouldDelegateToHandlerExceptionResolverWhenTokenIsInvalid() throws Exception {
        RuntimeException tokenException = new RuntimeException("Token invalid or expired");

        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isBlacklisted(TOKEN)).thenReturn(false);
        when(tokenService.getSubject(TOKEN)).thenThrow(tokenException);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(request, response, null, tokenException);
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Deve delegar exceção quando usuário não encontrado")
    void shouldDelegateExceptionWhenUserNotFound() throws Exception {
        RuntimeException userNotFoundException = new RuntimeException("User not found");

        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isBlacklisted(TOKEN)).thenReturn(false);
        when(tokenService.getSubject(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenThrow(userNotFoundException);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(request, response, null, userNotFoundException);
        verify(filterChain, never()).doFilter(any(), any());
    }

    // ==================== skipPaths ====================

    @Test
    @DisplayName("Deve ignorar filtro para rotas no skipPaths")
    void shouldSkipFilterForSkipPaths() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(securityProperties.getSkipPaths()).thenReturn(List.of("/api/auth/login"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenBlacklistService, never()).isBlacklisted(any());
    }

    @Test
    @DisplayName("Não deve ignorar filtro para rotas fora do skipPaths")
    void shouldNotSkipFilterForNonSkipPaths() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/employee/list");
        when(securityProperties.getSkipPaths()).thenReturn(List.of("/api/auth/login"));
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(tokenBlacklistService.isBlacklisted("valid-token")).thenReturn(false);
        when(tokenService.getSubject("valid-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(
            new User("user@test.com", "password", List.of())
        );

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenBlacklistService).isBlacklisted("valid-token");
    }

    @Test
    @DisplayName("Não deve ignorar filtro quando skipPaths está vazio")
    void shouldNotSkipWhenSkipPathsIsEmpty() throws Exception {
        when(securityProperties.getSkipPaths()).thenReturn(List.of());
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(tokenBlacklistService.isBlacklisted("valid-token")).thenReturn(false);
        when(tokenService.getSubject("valid-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(
            new User("user@test.com", "password", List.of())
        );

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenBlacklistService).isBlacklisted("valid-token");
    }
}