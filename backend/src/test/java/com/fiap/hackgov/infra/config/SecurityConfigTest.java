package com.fiap.hackgov.infra.config;

import com.fiap.hackgov.infra.filters.RateLimitFilter;
import com.fiap.hackgov.infra.filters.Filter;
import com.fiap.hackgov.infra.security.Security;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityConfigTest {

    @InjectMocks
    private Security security;

    @Mock
    private RateLimitFilter rateLimitFilter;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Test
    @DisplayName("Deve criar PasswordEncoder do tipo BCrypt")
    void shouldCreateBCryptPasswordEncoder() {
        PasswordEncoder encoder = security.passwordEncoder();

        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    @DisplayName("BCryptPasswordEncoder deve codificar e validar senha corretamente")
    void shouldEncodeAndMatchPassword() {
        PasswordEncoder encoder = security.passwordEncoder();
        String rawPassword = "minhasenha123";

        String encoded = encoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoder.matches(rawPassword, encoded));
    }

    @Test
    @DisplayName("BCryptPasswordEncoder não deve aceitar senha errada")
    void shouldNotMatchWrongPassword() {
        PasswordEncoder encoder = security.passwordEncoder();

        String encoded = encoder.encode("senhaCorreta");

        assertFalse(encoder.matches("senhaErrada", encoded));
    }

    @Test
    @DisplayName("Deve criar AuthenticationManager corretamente")
    void shouldCreateAuthenticationManager() throws Exception {
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager result = security.authenticationManager(authenticationConfiguration);

        assertNotNull(result);
        assertEquals(mockManager, result);
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
    }

    @Test
    @DisplayName("Deve criar SecurityFilter corretamente")
    void shouldCreateSecurityFilter() {
        Filter filter = security.securityFilter();

        assertNotNull(filter);
        assertInstanceOf(Filter.class, filter);
    }

    @Test
    @DisplayName("Duas senhas iguais devem gerar hashes diferentes")
    void shouldGenerateDifferentHashesForSamePassword() {
        PasswordEncoder encoder = security.passwordEncoder();
        String password = "mesmasenha";

        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);

        assertNotEquals(hash1, hash2); // BCrypt usa salt aleatório
        assertTrue(encoder.matches(password, hash1));
        assertTrue(encoder.matches(password, hash2));
    }
}