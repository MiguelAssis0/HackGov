package com.fiap.hackgov.services;

import com.fiap.hackgov.infra.exceptions.BlockedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    private static final String EMAIL = "user@test.com";
    private static final String IP    = "192.168.0.1";

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    @DisplayName("Não deve bloquear antes de 5 tentativas")
    void shouldNotBlockBeforeFiveAttempts() {
        for (int i = 0; i < 4; i++) {
            loginAttemptService.registerFailure(EMAIL, IP);
        }

        assertDoesNotThrow(() -> loginAttemptService.checkBlocked(EMAIL, IP));
    }

    @Test
    @DisplayName("Deve bloquear por 5 minutos após 5 tentativas falhas")
    void shouldBlockFiveMinutesAfterFiveAttempts() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.registerFailure(EMAIL, IP);
        }

        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(EMAIL, IP));

        assertTrue(ex.getMessage().contains("blocked"));
        assertTrue(ex.getMessage().contains("minute"));
    }

    @Test
    @DisplayName("Deve bloquear por 15 minutos após 10 tentativas falhas")
    void shouldBlockFifteenMinutesAfterTenAttempts() {
        for (int i = 0; i < 10; i++) {
            loginAttemptService.registerFailure(EMAIL, IP);
        }

        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(EMAIL, IP));

        assertTrue(ex.getMessage().contains("blocked"));
    }

    @Test
    @DisplayName("Deve bloquear por 1 hora após 15 tentativas falhas")
    void shouldBlockOneHourAfterFifteenAttempts() {
        for (int i = 0; i < 15; i++) {
            loginAttemptService.registerFailure(EMAIL, IP);
        }

        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(EMAIL, IP));

        assertTrue(ex.getMessage().contains("blocked"));
    }

    @Test
    @DisplayName("Deve bloquear permanentemente após 15+ tentativas")
    void shouldBlockPermanentlyAfterFifteenPlusAttempts() {
        for (int i = 0; i < 16; i++) {
            loginAttemptService.registerFailure(EMAIL, IP);
        }

        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(EMAIL, IP));

        assertTrue(ex.getMessage().contains("permanently blocked"));
        assertTrue(ex.getMessage().contains("support"));
    }

    @Test
    @DisplayName("Deve resetar tentativas após login bem-sucedido")
    void shouldResetAttemptsAfterSuccessfulLogin() {
        for (int i = 0; i < 4; i++) {
            loginAttemptService.registerFailure(EMAIL, IP);
        }

        loginAttemptService.registerSuccess(EMAIL, IP);

        assertDoesNotThrow(() -> loginAttemptService.checkBlocked(EMAIL, IP));
    }

    @Test
    @DisplayName("Deve bloquear por email independente do IP")
    void shouldBlockByEmailIndependentlyOfIp() {
        String differentIp = "10.0.0.1";

        for (int i = 0; i < 5; i++) {
            loginAttemptService.registerFailure(EMAIL, IP);
        }

        // Mesmo com IP diferente, o email está bloqueado
        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(EMAIL, differentIp));

        assertTrue(ex.getMessage().contains("account"));
    }

    @Test
    @DisplayName("Deve bloquear por IP independente do email")
    void shouldBlockByIpIndependentlyOfEmail() {
        String differentEmail = "other@test.com";

        for (int i = 0; i < 5; i++) {
            loginAttemptService.registerFailure(EMAIL, IP);
        }

        // Mesmo com email diferente, o IP está bloqueado
        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(differentEmail, IP));

        assertTrue(ex.getMessage().contains("IP"));
    }

    @Test
    @DisplayName("Emails diferentes devem ter contadores independentes")
    void shouldHaveIndependentCountersPerEmail() {
        String otherEmail = "other@test.com";

        for (int i = 0; i < 5; i++) {
            loginAttemptService.registerFailure(EMAIL, IP);
        }

        // Email diferente com IP diferente — não deve estar bloqueado
        assertDoesNotThrow(() -> loginAttemptService.checkBlocked(otherEmail, "10.0.0.2"));
    }

    @Test
    @DisplayName("Não deve bloquear sem nenhuma tentativa registrada")
    void shouldNotBlockWithNoAttempts() {
        assertDoesNotThrow(() -> loginAttemptService.checkBlocked(EMAIL, IP));
    }

    @Test
    @DisplayName("Mensagem de bloqueio permanente deve mencionar suporte")
    void shouldMentionSupportInPermanentBlockMessage() {
        for (int i = 0; i < 16; i++) {
            loginAttemptService.registerFailure(EMAIL, IP);
        }

        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(EMAIL, IP));

        assertTrue(ex.getMessage().toLowerCase().contains("support"));
    }
}
