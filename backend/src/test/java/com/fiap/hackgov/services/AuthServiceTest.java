package com.fiap.hackgov.services;

import com.fiap.hackgov.DTOs.Auth.LoginRequestDTO;
import com.fiap.hackgov.DTOs.Auth.LoginResponseDTO;
import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.infra.exceptions.BlockedException;
import com.fiap.hackgov.infra.exceptions.InvalidCredentialsException;
import com.fiap.hackgov.infra.security.TokenService;
import com.fiap.hackgov.repositories.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginAttemptService loginAttemptService;

    private static final String EMAIL     = "user@test.com";
    private static final String PASSWORD  = "senha123";
    private static final String IP        = "192.168.0.1";
    private static final String TOKEN     = "jwt-token-mock";

    private Employee mockEmployee(boolean twoFactor) {
        Employee employee = mock(Employee.class);
        when(employee.getEmail()).thenReturn(EMAIL);
        when(employee.getName()).thenReturn("Test User");
        when(employee.getPassword()).thenReturn("encodedPassword");
        when(employee.isStatus()).thenReturn(true);
        when(employee.isTwoFactor()).thenReturn(twoFactor);
        return employee;
    }

    @Test
    @DisplayName("Deve realizar login com sucesso sem 2FA")
    void shouldLoginSuccessfullyWithoutTwoFactor() {
        Employee employee = mockEmployee(false);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(PASSWORD, "encodedPassword")).thenReturn(true);
        when(tokenService.generateToken(employee)).thenReturn(TOKEN);

        LoginResponseDTO response = authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP);

        assertNotNull(response.token());
        assertFalse(response.requiresTwoFactor());
        verify(loginAttemptService).registerSuccess(EMAIL, IP);
        verify(loginAttemptService).checkBlocked(EMAIL, IP);
    }

    @Test
    @DisplayName("Deve registrar falha quando email não encontrado")
    void shouldRegisterFailureWhenEmailNotFound() {
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP));

        verify(loginAttemptService).registerFailure(EMAIL, IP);
        verify(loginAttemptService, never()).registerSuccess(any(), any());
    }

    @Test
    @DisplayName("Deve registrar falha quando senha incorreta")
    void shouldRegisterFailureWhenWrongPassword() {
        Employee employee = mock(Employee.class);
        when(employee.isStatus()).thenReturn(true);
        when(employee.getPassword()).thenReturn("encodedPassword");

        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(PASSWORD, "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP));

        verify(loginAttemptService).registerFailure(EMAIL, IP);
        verify(loginAttemptService, never()).registerSuccess(any(), any());
    }

    @Test
    @DisplayName("Deve lançar BlockedException quando bloqueado")
    void shouldThrowBlockedExceptionWhenBlocked() {
        doThrow(new BlockedException("Your account is blocked. Try again in 5 minute(s)."))
                .when(loginAttemptService).checkBlocked(EMAIL, IP);

        BlockedException ex = assertThrows(BlockedException.class,
                () -> authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP));

        assertTrue(ex.getMessage().contains("blocked"));
        verify(employeeRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Deve retornar requiresTwoFactor true quando 2FA habilitado")
    void shouldReturnRequiresTwoFactorWhenEnabled() {
        Employee employee = mockEmployee(true);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(PASSWORD, "encodedPassword")).thenReturn(true);

        LoginResponseDTO response = authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP);

        assertNull(response.token());
        assertTrue(response.requiresTwoFactor());
        verify(twoFactorAuthService).sendTwoFactorCode(EMAIL, "Test User");
        verify(loginAttemptService).registerSuccess(EMAIL, IP);
    }

    @Test
    @DisplayName("Não deve registrar falha quando conta está inativa")
    void shouldNotRegisterFailureWhenAccountInactive() {
        Employee employee = mock(Employee.class);
        when(employee.isStatus()).thenReturn(false);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP));

        // Conta inativa não conta como tentativa de brute force
        verify(loginAttemptService, never()).registerFailure(any(), any());
    }

    @Test
    @DisplayName("Deve resetar tentativas após login bem-sucedido")
    void shouldResetAttemptsAfterSuccessfulLogin() {
        Employee employee = mockEmployee(false);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(PASSWORD, "encodedPassword")).thenReturn(true);
        when(tokenService.generateToken(employee)).thenReturn(TOKEN);

        authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP);

        verify(loginAttemptService).registerSuccess(EMAIL, IP);
        verify(loginAttemptService, never()).registerFailure(any(), any());
    }
}