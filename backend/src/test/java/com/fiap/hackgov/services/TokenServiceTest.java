package com.fiap.hackgov.services;

import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.entities.enums.Roles;
import com.fiap.hackgov.infra.security.TokenService;
import com.fiap.hackgov.infra.exceptions.TokenInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(UUID.randomUUID());
        testEmployee.setName("Test User");
        testEmployee.setEmail("test@example.com");
        testEmployee.setRole(Roles.EMPLOYEE);
        
        ReflectionTestUtils.setField(tokenService, "SECRET_KEY", "test-secret-key-for-jwt-token-generation");
    }

    @Test
    void generateToken_Success() {
        String token = tokenService.generateToken(testEmployee);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);


        assertNotNull(request);
        assertFalse(request.getHeader("Authorization").isEmpty());
        assertEquals("Bearer " + token, request.getHeader("Authorization"));
    }

    @Test
    void validateToken_Success() {
        String token = tokenService.generateToken(testEmployee);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        String subject = tokenService.validateToken(request);

        assertEquals("Test User", subject);
    }

    @Test
    void validateToken_InvalidToken_ThrowsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        // token assinado com chave diferente
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.invalidsignature";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);

        assertThrows(TokenInvalidException.class, () -> tokenService.validateToken(request));
    }

    @Test
    void getSubject_Success() {
        String token = tokenService.generateToken(testEmployee);
        String subject = tokenService.getSubject(token);

        assertEquals("Test User", subject);
    }

    @Test
    void getSubject_InvalidToken_ThrowsException() {
        String invalidToken = "invalid.jwt.token";

        assertThrows(TokenInvalidException.class, () -> tokenService.getSubject(invalidToken));
    }

    @Test
    void getToken_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token");

        String token = tokenService.extractToken(request);

        assertEquals("jwt-token", token);
    }

    @Test
    void getToken_NoAuthorizationHeader_ReturnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        String token = tokenService.extractToken(request);

        assertNull(token);
    }

    @Test
    void getToken_InvalidHeaderFormat_ReturnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat jwt-token");

        String token = tokenService.extractToken(request);

        assertNull(token);
    }
}
