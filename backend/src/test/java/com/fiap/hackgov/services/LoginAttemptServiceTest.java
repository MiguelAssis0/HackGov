package com.fiap.hackgov.services;

import com.fiap.hackgov.shared.infra.entities.BlockedAttempt;
import com.fiap.hackgov.shared.infra.exceptions.BlockedException;
import com.fiap.hackgov.shared.infra.repositories.BlockedAttemptRepository;
import com.fiap.hackgov.shared.infra.services.LoginAttemptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginAttemptServiceTest {

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    @Mock
    private BlockedAttemptRepository blockedAttemptRepository;

    private static final String IP = "192.168.0.1";

    private BlockedAttempt attemptWithCount(int count) {
        BlockedAttempt data = new BlockedAttempt("ip:192.168.0.1");
        data.setTotalAttempts(count);
        return data;
    }

    private BlockedAttempt permanentlyBlocked() {
        BlockedAttempt data = new BlockedAttempt("ip:192.168.0.1");
        data.setTotalAttempts(15);
        data.setPermanentlyBlocked(true);
        return data;
    }

    private BlockedAttempt blockedUntil(String key, int attempts, LocalDateTime until) {
        BlockedAttempt data = new BlockedAttempt(key);
        data.setTotalAttempts(attempts);
        data.setBlockedUntil(until);
        return data;
    }

    @Test
    @DisplayName("Não deve bloquear sem tentativas registradas")
    void shouldNotBlockWithNoAttempts() {
        when(blockedAttemptRepository.findByKey("ip:" + IP)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> loginAttemptService.checkBlocked(IP));
    }

    @Test
    @DisplayName("Não deve bloquear com menos de 5 tentativas")
    void shouldNotBlockBeforeFiveAttempts() {
        when(blockedAttemptRepository.findByKey("ip:" + IP))
                .thenReturn(Optional.of(attemptWithCount(4)));

        assertDoesNotThrow(() -> loginAttemptService.checkBlocked(IP));
    }

    @Test
    @DisplayName("Deve bloquear por 5 minutos após 5 tentativas")
    void shouldBlockFiveMinutesAfterFiveAttempts() {
        when(blockedAttemptRepository.findByKey("ip:" + IP))
                .thenReturn(Optional.of(blockedUntil("ip:" + IP, 5, LocalDateTime.now().plusMinutes(5))));

        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(IP));

        assertTrue(ex.getMessage().contains("blocked"));
        assertTrue(ex.getMessage().contains("minute"));
    }

    @Test
    @DisplayName("Deve bloquear por 15 minutos após 10 tentativas")
    void shouldBlockFifteenMinutesAfterTenAttempts() {
        when(blockedAttemptRepository.findByKey("ip:" + IP))
                .thenReturn(Optional.of(blockedUntil("ip:" + IP, 10, LocalDateTime.now().plusMinutes(15))));

        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(IP));

        assertTrue(ex.getMessage().contains("blocked"));
    }

    @Test
    @DisplayName("Deve bloquear por 1 hora após 15 tentativas")
    void shouldBlockOneHourAfterFifteenAttempts() {
        when(blockedAttemptRepository.findByKey("ip:" + IP))
                .thenReturn(Optional.of(blockedUntil("ip:" + IP, 15, LocalDateTime.now().plusMinutes(60))));

        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(IP));

        assertTrue(ex.getMessage().contains("blocked"));
    }

    @Test
    @DisplayName("Deve bloquear permanentemente após 15+ tentativas")
    void shouldBlockPermanentlyAfterFifteenPlusAttempts() {
        when(blockedAttemptRepository.findByKey("ip:" + IP))
                .thenReturn(Optional.of(attemptWithCount(15)));

        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(IP));

        assertTrue(ex.getMessage().contains("permanently blocked"));
        assertTrue(ex.getMessage().contains("support"));
    }

    @Test
    @DisplayName("Deve manter bloqueio permanente")
    void shouldKeepPermanentBlock() {
        when(blockedAttemptRepository.findByKey("ip:" + IP))
                .thenReturn(Optional.of(permanentlyBlocked()));

        BlockedException ex = assertThrows(BlockedException.class,
                () -> loginAttemptService.checkBlocked(IP));

        assertTrue(ex.getMessage().contains("permanently blocked"));
    }

    @Test
    @DisplayName("Deve incrementar tentativas ao registrar falha")
    void shouldIncrementAttemptsOnFailure() {
        when(blockedAttemptRepository.findByKey("ip:" + IP)).thenReturn(Optional.empty());

        loginAttemptService.registerFailure(IP);

        verify(blockedAttemptRepository).save(argThat(a ->
                a.getKey().equals("ip:" + IP) && a.getTotalAttempts() == 1
        ));
    }

    @Test
    @DisplayName("Deve resetar tentativas após sucesso")
    void shouldResetAttemptsOnSuccess() {
        when(blockedAttemptRepository.findByKey("ip:" + IP))
                .thenReturn(Optional.of(attemptWithCount(3)));

        loginAttemptService.registerSuccess(IP);

        verify(blockedAttemptRepository).save(argThat(a ->
                a.getTotalAttempts() == 0 && !a.isPermanentlyBlocked()
        ));
    }

    @Test
    @DisplayName("Não deve falhar ao resetar IP sem tentativas registradas")
    void shouldNotFailOnResetWithNoAttempts() {
        when(blockedAttemptRepository.findByKey("ip:" + IP)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> loginAttemptService.registerSuccess(IP));
        verify(blockedAttemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("IPs diferentes devem ter contadores independentes")
    void shouldHaveIndependentCountersPerIp() {
        when(blockedAttemptRepository.findByKey("ip:10.0.0.2")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> loginAttemptService.checkBlocked("10.0.0.2"));
    }

    @Test
    @DisplayName("Deve registrar falha no 2FA separadamente do login")
    void shouldRegisterTwoFactorFailureSeparately() {
        when(blockedAttemptRepository.findByKey("2fa-ip:" + IP)).thenReturn(Optional.empty());

        loginAttemptService.registerTwoFactorFailure(IP);

        verify(blockedAttemptRepository).save(argThat(a ->
                a.getKey().equals("2fa-ip:" + IP)
        ));
    }

    @Test
    @DisplayName("Deve bloquear 2FA após 5 tentativas sem afetar login")
    void shouldBlockTwoFactorWithoutAffectingLogin() {
        when(blockedAttemptRepository.findByKey("2fa-ip:" + IP))
                .thenReturn(Optional.of(blockedUntil("2fa-ip:" + IP, 5, LocalDateTime.now().plusMinutes(5))));
        when(blockedAttemptRepository.findByKey("ip:" + IP)).thenReturn(Optional.empty());

        assertThrows(BlockedException.class,
                () -> loginAttemptService.checkTwoFactorBlocked(IP));

        assertDoesNotThrow(() -> loginAttemptService.checkBlocked(IP));
    }

    @Test
    @DisplayName("Deve chamar deleteIrrelevantOldRecords no scheduled")
    void shouldCallCleanupOnSchedule() {
        loginAttemptService.cleanIrrelevantRecords();

        verify(blockedAttemptRepository).deleteIrrelevantOldRecords(any(LocalDateTime.class));
    }
}