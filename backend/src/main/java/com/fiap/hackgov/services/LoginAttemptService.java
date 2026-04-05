package com.fiap.hackgov.services;

import com.fiap.hackgov.entities.BlockedAttempt;
import com.fiap.hackgov.infra.exceptions.BlockedException;
import com.fiap.hackgov.repositories.BlockedAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private static final int FIRST_BLOCK_ATTEMPTS     = 5;
    private static final int SECOND_BLOCK_ATTEMPTS    = 10;
    private static final int PERMANENT_BLOCK_ATTEMPTS = 15;

    private static final int FIRST_BLOCK_MINUTES  = 5;
    private static final int SECOND_BLOCK_MINUTES = 15;
    private static final int THIRD_BLOCK_MINUTES  = 60;

    @Autowired
    private BlockedAttemptRepository blockedAttemptRepository;

    // ==================== Login ====================

    @Transactional
    public void registerFailure(String ip) {
        increment("ip:" + ip);
    }

    @Transactional
    public void registerSuccess(String ip) {
        reset("ip:" + ip);
    }

    public void checkBlocked(String ip) {
        checkKey("ip:" + ip, "Your IP address");
    }

    // ==================== 2FA ====================

    @Transactional
    public void registerTwoFactorFailure(String ip) {
        increment("2fa-ip:" + ip);
    }

    @Transactional
    public void registerTwoFactorSuccess(String ip) {
        reset("2fa-ip:" + ip);
    }

    public void checkTwoFactorBlocked(String ip) {
        checkKey("2fa-ip:" + ip, "Your IP address");
    }

    // ==================== Internos ====================

    private void increment(String key) {
        BlockedAttempt data = blockedAttemptRepository.findByKey(key)
                .orElse(new BlockedAttempt(key));

        if (data.isPermanentlyBlocked()) return;

        if (data.getBlockedUntil() != null && data.getBlockedUntil().isBefore(LocalDateTime.now())) {
            data.setBlockedUntil(null);
        }

        data.setTotalAttempts(data.getTotalAttempts() + 1);
        data.setUpdatedAt(LocalDateTime.now());
        blockedAttemptRepository.save(data);
    }

    private void reset(String key) {
        blockedAttemptRepository.findByKey(key).ifPresent(data -> {
            data.setTotalAttempts(0);
            data.setBlockedUntil(null);
            data.setPermanentlyBlocked(false);
            data.setUpdatedAt(LocalDateTime.now());
            blockedAttemptRepository.save(data);
        });
    }

    private void checkKey(String key, String subject) {
        BlockedAttempt data = blockedAttemptRepository.findByKey(key).orElse(null);
        if (data == null) return;

        if (data.isPermanentlyBlocked()) {
            throw new BlockedException(subject + " is permanently blocked. Please contact support.");
        }

        if (data.getTotalAttempts() >= PERMANENT_BLOCK_ATTEMPTS) {
            data.setPermanentlyBlocked(true);
            data.setUpdatedAt(LocalDateTime.now());
            blockedAttemptRepository.save(data);
            throw new BlockedException(subject + " is permanently blocked. Please contact support.");
        }

        if (data.getTotalAttempts() >= SECOND_BLOCK_ATTEMPTS) {
            applyOrCheckTemporaryBlock(data, THIRD_BLOCK_MINUTES, subject);
            return;
        }

        if (data.getTotalAttempts() >= FIRST_BLOCK_ATTEMPTS) {
            applyOrCheckTemporaryBlock(data, SECOND_BLOCK_MINUTES, subject);
            return;
        }

        if (data.getBlockedUntil() != null && data.getBlockedUntil().isAfter(LocalDateTime.now())) {
            long minutesLeft = java.time.Duration.between(LocalDateTime.now(), data.getBlockedUntil()).toMinutes() + 1;
            throw new BlockedException(subject + " is blocked. Try again in " + minutesLeft + " minute(s).");
        }
    }

    private void applyOrCheckTemporaryBlock(BlockedAttempt data, int minutes, String subject) {
        if (data.getBlockedUntil() == null || data.getBlockedUntil().isBefore(LocalDateTime.now())) {
            data.setBlockedUntil(LocalDateTime.now().plusMinutes(minutes));
            data.setUpdatedAt(LocalDateTime.now());
            blockedAttemptRepository.save(data);
        }
        long minutesLeft = java.time.Duration.between(LocalDateTime.now(), data.getBlockedUntil()).toMinutes() + 1;
        throw new BlockedException(subject + " is blocked. Try again in " + minutesLeft + " minute(s).");
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanIrrelevantRecords() {
        blockedAttemptRepository.deleteIrrelevantOldRecords(LocalDateTime.now().minusDays(7));
    }
}