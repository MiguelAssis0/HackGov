package com.fiap.hackgov.services;

import com.fiap.hackgov.DTOs.Auth.LoginRequestDTO;
import com.fiap.hackgov.DTOs.Auth.LoginResponseDTO;
import com.fiap.hackgov.DTOs.Auth.TwoFactorRequestDTO;
import com.fiap.hackgov.DTOs.Auth.TwoFactorResponseDTO;
import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.entities.enums.Roles;
import com.fiap.hackgov.infra.exceptions.InvalidCredentialsException;
import com.fiap.hackgov.infra.security.TokenService;
import com.fiap.hackgov.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Spy
    private PasswordEncoder passwordEncoder =
            Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    @InjectMocks
    private AuthService authService;

    private Employee testEmployee;
    private LoginRequestDTO loginRequest;
    private TwoFactorRequestDTO twoFactorRequest;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(UUID.randomUUID());
        testEmployee.setName("Test User");
        testEmployee.setEmail("test@example.com");
        testEmployee.setPassword(passwordEncoder.encode("password123"));
        testEmployee.setRole(Roles.EMPLOYEE);
        testEmployee.setStatus(true);
        testEmployee.setTwoFactor(false);

        loginRequest = new LoginRequestDTO("test@example.com", "password123");
        twoFactorRequest = new TwoFactorRequestDTO("test@example.com", "123456");
    }

    @Test
    void login_Success_Without2FA() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));
        when(tokenService.generateToken(any(Employee.class))).thenReturn("jwt-token");

        LoginResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("test@example.com", response.email());
        assertEquals("Test User", response.name());
        assertEquals(Roles.EMPLOYEE, response.role());
        assertFalse(response.requiresTwoFactor());
        
        verify(employeeRepository).save(testEmployee);
        verify(twoFactorAuthService, never()).sendTwoFactorCode(anyString(), anyString());
    }

    @Test
    void login_Success_With2FA() {
        testEmployee.setTwoFactor(true);
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        LoginResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertNull(response.token());
        assertEquals("test@example.com", response.email());
        assertEquals("Test User", response.name());
        assertEquals(Roles.EMPLOYEE, response.role());
        assertTrue(response.requiresTwoFactor());
        
        verify(twoFactorAuthService).sendTwoFactorCode("test@example.com", "Test User");
        verify(tokenService, never()).generateToken(any(Employee.class));
    }

    @Test
    void login_InvalidEmail_ThrowsException() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
        
        verify(tokenService, never()).generateToken(any(Employee.class));
        verify(twoFactorAuthService, never()).sendTwoFactorCode(anyString(), anyString());
    }

    @Test
    void login_InactiveUser_ThrowsException() {
        testEmployee.setStatus(false);
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
        
        verify(tokenService, never()).generateToken(any(Employee.class));
        verify(twoFactorAuthService, never()).sendTwoFactorCode(anyString(), anyString());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        LoginRequestDTO wrongPasswordRequest = new LoginRequestDTO("test@example.com", "wrongpassword");
        
        assertThrows(InvalidCredentialsException.class, () -> authService.login(wrongPasswordRequest));
        
        verify(tokenService, never()).generateToken(any(Employee.class));
        verify(twoFactorAuthService, never()).sendTwoFactorCode(anyString(), anyString());
    }

    @Test
    void verifyTwoFactor_Success() {
        testEmployee.setTwoFactor(true);
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));
        when(twoFactorAuthService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(tokenService.generateToken(any(Employee.class))).thenReturn("jwt-token");

        TwoFactorResponseDTO response = authService.verifyTwoFactor(twoFactorRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("Two-factor authentication successful", response.message());
        
        verify(twoFactorAuthService).verifyCode("test@example.com", "123456");
        verify(tokenService).generateToken(testEmployee);
    }

    @Test
    void verifyTwoFactor_InvalidEmail_ThrowsException() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.verifyTwoFactor(twoFactorRequest));
        
        verify(twoFactorAuthService, never()).verifyCode(anyString(), anyString());
        verify(tokenService, never()).generateToken(any(Employee.class));
    }

    @Test
    void verifyTwoFactor_TwoFactorDisabled_ThrowsException() {
        testEmployee.setTwoFactor(false);
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        assertThrows(InvalidCredentialsException.class, () -> authService.verifyTwoFactor(twoFactorRequest));
        
        verify(twoFactorAuthService, never()).verifyCode(anyString(), anyString());
        verify(tokenService, never()).generateToken(any(Employee.class));
    }

    @Test
    void verifyTwoFactor_InvalidCode_ThrowsException() {
        testEmployee.setTwoFactor(true);
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));
        when(twoFactorAuthService.verifyCode(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.verifyTwoFactor(twoFactorRequest));
        
        verify(twoFactorAuthService).verifyCode("test@example.com", "123456");
        verify(tokenService, never()).generateToken(any(Employee.class));
    }
}
