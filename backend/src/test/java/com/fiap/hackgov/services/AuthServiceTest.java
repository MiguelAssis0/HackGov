package com.fiap.hackgov.services;

import com.fiap.hackgov.DTOs.Auth.LoginRequestDTO;
import com.fiap.hackgov.DTOs.Auth.LoginResponseDTO;
import com.fiap.hackgov.DTOs.Auth.RefreshToken.RefreshTokenRequestDTO;
import com.fiap.hackgov.DTOs.Auth.RefreshToken.RefreshTokenResponseDTO;
import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.infra.exceptions.BlockedException;
import com.fiap.hackgov.infra.exceptions.InvalidCredentialsException;
import com.fiap.hackgov.infra.security.TokenService;
import com.fiap.hackgov.infra.utils.AuditLog;
import com.fiap.hackgov.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

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

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private AuditLog auditLog;

    @Mock
    private AuditLog.Builder builderMock;

    private static final String EMAIL         = "user@test.com";
    private static final String PASSWORD      = "senha123";
    private static final String IP            = "192.168.0.1";
    private static final String ACCESS_TOKEN  = "access-token-mock";
    private static final String REFRESH_TOKEN = "refresh-token-mock";

    @BeforeEach
    void setUp() {
        lenient().when(auditLog.with(any())).thenReturn(builderMock);
        lenient().when(builderMock.event(any())).thenReturn(builderMock);
        lenient().when(builderMock.email(any())).thenReturn(builderMock);
        lenient().when(builderMock.reason(any())).thenReturn(builderMock);
        lenient().when(builderMock.level(any())).thenReturn(builderMock);
    }

    private Employee mockFullEmployee(boolean twoFactor) {
        Employee employee = mock(Employee.class);
        when(employee.getEmail()).thenReturn(EMAIL);
        when(employee.getName()).thenReturn("Test User");
        when(employee.getPassword()).thenReturn("encodedPassword");
        when(employee.isStatus()).thenReturn(true);
        when(employee.isTwoFactor()).thenReturn(twoFactor);
        return employee;
    }

    // ==================== login ====================

    @Test
    @DisplayName("Deve realizar login com sucesso sem 2FA")
    void shouldLoginSuccessfullyWithoutTwoFactor() {
        Employee employee = mockFullEmployee(false);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(PASSWORD, "encodedPassword")).thenReturn(true);
        when(tokenService.generateToken(employee)).thenReturn(ACCESS_TOKEN);
        when(tokenService.generateRefreshToken(employee)).thenReturn(REFRESH_TOKEN);

        LoginResponseDTO response = authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP);

        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertFalse(response.requiresTwoFactor());
        verify(loginAttemptService).registerSuccess(IP);
        verify(loginAttemptService).checkBlocked(IP);
    }

    @Test
    @DisplayName("Deve registrar falha quando email não encontrado")
    void shouldRegisterFailureWhenEmailNotFound() {
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP));

        verify(loginAttemptService).registerFailure(IP);
        verify(loginAttemptService, never()).registerSuccess(any());
    }

    @Test
    @DisplayName("Deve registrar falha quando senha incorreta")
    void shouldRegisterFailureWhenWrongPassword() {
        Employee employee = mock(Employee.class);
        when(employee.getPassword()).thenReturn("encodedPassword");
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(PASSWORD, "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP));

        verify(loginAttemptService).registerFailure(IP);
        verify(loginAttemptService, never()).registerSuccess(any());
    }

    @Test
    @DisplayName("Deve lançar BlockedException quando IP bloqueado")
    void shouldThrowBlockedExceptionWhenBlocked() {
        doThrow(new BlockedException("Your IP address is blocked. Try again in 5 minute(s)."))
                .when(loginAttemptService).checkBlocked(IP);

        BlockedException ex = assertThrows(BlockedException.class,
                () -> authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP));

        assertTrue(ex.getMessage().contains("blocked"));
        verify(employeeRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Deve retornar requiresTwoFactor true e não gerar token quando 2FA habilitado")
    void shouldReturnRequiresTwoFactorWhenEnabled() {
        Employee employee = mockFullEmployee(true);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(PASSWORD, "encodedPassword")).thenReturn(true);

        LoginResponseDTO response = authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP);

        assertNull(response.accessToken());
        assertNull(response.refreshToken());
        assertTrue(response.requiresTwoFactor());
        verify(twoFactorAuthService).sendTwoFactorCode(EMAIL, "Test User");
        verify(loginAttemptService).registerSuccess(IP);
        verify(tokenService, never()).generateToken(any());
        verify(tokenService, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("Não deve registrar falha quando conta está inativa")
    void shouldNotRegisterFailureWhenAccountInactive() {
        Employee employee = mock(Employee.class);
        when(employee.getPassword()).thenReturn("encodedPassword");
        when(employee.isStatus()).thenReturn(false);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(PASSWORD, "encodedPassword")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP));

        verify(loginAttemptService, never()).registerFailure(any());
    }

    @Test
    @DisplayName("Deve resetar tentativas após login bem-sucedido")
    void shouldResetAttemptsAfterSuccessfulLogin() {
        Employee employee = mockFullEmployee(false);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(PASSWORD, "encodedPassword")).thenReturn(true);
        when(tokenService.generateToken(employee)).thenReturn(ACCESS_TOKEN);
        when(tokenService.generateRefreshToken(employee)).thenReturn(REFRESH_TOKEN);

        authService.login(new LoginRequestDTO(EMAIL, PASSWORD), IP);

        verify(loginAttemptService).registerSuccess(IP);
        verify(loginAttemptService, never()).registerFailure(any());
    }

    // ==================== logout ====================

    @Test
    @DisplayName("Deve blacklistar token no logout")
    void shouldBlacklistTokenOnLogout() {
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);
        when(tokenService.getExpirationAsLocalDateTime(ACCESS_TOKEN)).thenReturn(expiration);

        authService.logout(ACCESS_TOKEN);

        verify(tokenBlacklistService).blacklist(ACCESS_TOKEN, expiration);
    }

    @Test
    @DisplayName("Deve usar a expiração correta do token ao fazer logout")
    void shouldUseCorrectExpirationOnLogout() {
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(10);
        when(tokenService.getExpirationAsLocalDateTime(ACCESS_TOKEN)).thenReturn(expiration);

        authService.logout(ACCESS_TOKEN);

        verify(tokenBlacklistService).blacklist(eq(ACCESS_TOKEN), eq(expiration));
    }

    // ==================== refreshToken ====================

    @Test
    @DisplayName("Deve gerar novos tokens ao fazer refresh com token válido")
    void shouldGenerateNewTokensOnRefresh() {
        Employee employee = new Employee();
        employee.setEmail(EMAIL);
        employee.setName("Test User");
        employee.setPassword("encodedPassword");
        employee.setStatus(true);
        employee.setTwoFactor(false);
        when(tokenService.getSubjectFromRefreshToken(REFRESH_TOKEN)).thenReturn(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(tokenService.generateToken(employee)).thenReturn("new-access-token");
        when(tokenService.generateRefreshToken(employee)).thenReturn("new-refresh-token");

        RefreshTokenResponseDTO response = authService.refreshToken(
                new RefreshTokenRequestDTO(REFRESH_TOKEN));

        assertNotNull(response.token());
        assertNotNull(response.refreshToken());
        assertEquals("new-access-token", response.token());
        assertEquals("new-refresh-token", response.refreshToken());
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer refresh com conta inativa")
    void shouldThrowWhenRefreshingWithInactiveAccount() {
        // Mock mínimo — só isStatus() é verificado nesse fluxo
        Employee employee = mock(Employee.class);
        when(employee.isStatus()).thenReturn(false);
        when(tokenService.getSubjectFromRefreshToken(REFRESH_TOKEN)).thenReturn(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.refreshToken(new RefreshTokenRequestDTO(REFRESH_TOKEN)));

        verify(tokenService, never()).generateToken(any());
        verify(tokenService, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer refresh com email não encontrado")
    void shouldThrowWhenRefreshingWithUnknownEmail() {
        when(tokenService.getSubjectFromRefreshToken(REFRESH_TOKEN)).thenReturn(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.refreshToken(new RefreshTokenRequestDTO(REFRESH_TOKEN)));

        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes no refresh")
    void shouldGenerateDifferentTokensOnRefresh() {
        Employee employee = new Employee();
        employee.setEmail(EMAIL);
        employee.setName("Test User");
        employee.setPassword("encodedPassword");
        employee.setStatus(true);
        employee.setTwoFactor(false);
        when(tokenService.getSubjectFromRefreshToken(REFRESH_TOKEN)).thenReturn(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(tokenService.generateToken(employee)).thenReturn("new-access-token");
        when(tokenService.generateRefreshToken(employee)).thenReturn("new-refresh-token");

        RefreshTokenResponseDTO response = authService.refreshToken(
                new RefreshTokenRequestDTO(REFRESH_TOKEN));

        assertNotEquals(REFRESH_TOKEN, response.refreshToken());
        assertNotEquals(ACCESS_TOKEN, response.token());
    }
}