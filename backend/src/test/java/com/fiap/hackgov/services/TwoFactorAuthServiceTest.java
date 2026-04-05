package com.fiap.hackgov.services;

import com.fiap.hackgov.entities.TwoFactorCode;
import com.fiap.hackgov.infra.utils.AuditLog;
import com.fiap.hackgov.repositories.TwoFactorCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TwoFactorAuthServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TwoFactorCodeRepository twoFactorCodeRepository;

    @Mock
    private AuditLog auditLog;

    @Mock
    private AuditLog.Builder builderMock;

    @InjectMocks
    private TwoFactorAuthService twoFactorAuthService;

    private static final String EMAIL = "test@example.com";
    private static final String NAME  = "Test User";
    private static final String CODE  = "123456";

    @BeforeEach
    void setUp() {
        lenient().when(auditLog.with(any())).thenReturn(builderMock);
        lenient().when(builderMock.event(any())).thenReturn(builderMock);
        lenient().when(builderMock.email(any())).thenReturn(builderMock);
        lenient().when(builderMock.reason(any())).thenReturn(builderMock);
        lenient().when(builderMock.level(any())).thenReturn(builderMock);
    }

    @Test
    @DisplayName("Deve gerar código com 6 dígitos numéricos")
    void shouldGenerateSixDigitCode() {
        String code = twoFactorAuthService.generateCode();

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    @DisplayName("Deve salvar código no banco e enviar email com sucesso")
    void shouldSaveCodeAndSendEmail() {
        when(twoFactorCodeRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        twoFactorAuthService.sendTwoFactorCode(EMAIL, NAME);

        verify(twoFactorCodeRepository).save(any(TwoFactorCode.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Deve sobrescrever código existente ao reenviar")
    void shouldOverwriteExistingCodeOnResend() {
        TwoFactorCode existing = new TwoFactorCode(EMAIL, "654321", LocalDateTime.now().plusMinutes(5));
        when(twoFactorCodeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        twoFactorAuthService.sendTwoFactorCode(EMAIL, NAME);

        ArgumentCaptor<TwoFactorCode> captor = ArgumentCaptor.forClass(TwoFactorCode.class);
        verify(twoFactorCodeRepository).save(captor.capture());

        assertNotEquals("654321", captor.getValue().getCode());
    }

    @Test
    @DisplayName("Deve deletar código do banco se falhar ao enviar email")
    void shouldDeleteCodeIfEmailFails() {
        when(twoFactorCodeRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Mail service failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class,
                () -> twoFactorAuthService.sendTwoFactorCode(EMAIL, NAME));

        verify(twoFactorCodeRepository).deleteByEmail(EMAIL);
    }

    @Test
    @DisplayName("Deve salvar código com expiração de 10 minutos")
    void shouldSaveCodeWithTenMinuteExpiration() {
        when(twoFactorCodeRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        twoFactorAuthService.sendTwoFactorCode(EMAIL, NAME);

        ArgumentCaptor<TwoFactorCode> captor = ArgumentCaptor.forClass(TwoFactorCode.class);
        verify(twoFactorCodeRepository).save(captor.capture());

        LocalDateTime expiration = captor.getValue().getExpiration();
        assertTrue(expiration.isAfter(LocalDateTime.now().plusMinutes(9)));
        assertTrue(expiration.isBefore(LocalDateTime.now().plusMinutes(11)));
    }

    @Test
    @DisplayName("Deve retornar true para código válido")
    void shouldReturnTrueForValidCode() {
        TwoFactorCode stored = new TwoFactorCode(EMAIL, CODE, LocalDateTime.now().plusMinutes(10));
        when(twoFactorCodeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(stored));

        boolean result = twoFactorAuthService.verifyCode(EMAIL, CODE);

        assertTrue(result);
        verify(twoFactorCodeRepository).deleteByEmail(EMAIL);
    }

    @Test
    @DisplayName("Deve retornar false quando código não encontrado")
    void shouldReturnFalseWhenCodeNotFound() {
        when(twoFactorCodeRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        boolean result = twoFactorAuthService.verifyCode(EMAIL, CODE);

        assertFalse(result);
        verify(twoFactorCodeRepository, never()).deleteByEmail(any());
    }

    @Test
    @DisplayName("Deve retornar false e deletar quando código expirado")
    void shouldReturnFalseAndDeleteWhenExpired() {
        TwoFactorCode expired = new TwoFactorCode(EMAIL, CODE, LocalDateTime.now().minusMinutes(1));
        when(twoFactorCodeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(expired));

        boolean result = twoFactorAuthService.verifyCode(EMAIL, CODE);

        assertFalse(result);
        verify(twoFactorCodeRepository).deleteByEmail(EMAIL);
    }

    @Test
    @DisplayName("Deve retornar false para código incorreto")
    void shouldReturnFalseForWrongCode() {
        TwoFactorCode stored = new TwoFactorCode(EMAIL, CODE, LocalDateTime.now().plusMinutes(10));
        when(twoFactorCodeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(stored));

        boolean result = twoFactorAuthService.verifyCode(EMAIL, "999999");

        assertFalse(result);
        verify(twoFactorCodeRepository, never()).deleteByEmail(any());
    }

    @Test
    @DisplayName("Não deve permitir reutilizar código após verificação bem-sucedida")
    void shouldNotAllowCodeReuseAfterVerification() {
        TwoFactorCode stored = new TwoFactorCode(EMAIL, CODE, LocalDateTime.now().plusMinutes(10));
        when(twoFactorCodeRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(stored))
                .thenReturn(Optional.empty());

        twoFactorAuthService.verifyCode(EMAIL, CODE);
        boolean secondAttempt = twoFactorAuthService.verifyCode(EMAIL, CODE);

        assertFalse(secondAttempt);
    }

    @Test
    @DisplayName("Deve deletar códigos expirados do banco")
    void shouldDeleteExpiredCodesFromDatabase() {
        twoFactorAuthService.cleanExpiredCodes();

        verify(twoFactorCodeRepository).deleteAllExpired(any(LocalDateTime.class));
    }
}