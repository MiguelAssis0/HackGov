package com.fiap.hackgov.services;

import com.fiap.hackgov.infra.exceptions.BlockedException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginAttemptService {

    private static final int FIRST_BLOCK_ATTEMPTS   = 5;
    private static final int SECOND_BLOCK_ATTEMPTS  = 10;
    private static final int PERMANENT_BLOCK_ATTEMPTS = 15;

    private static final int FIRST_BLOCK_MINUTES   = 5;
    private static final int SECOND_BLOCK_MINUTES  = 15;
    private static final int THIRD_BLOCK_MINUTES   = 60;

    private final ConcurrentMap<String, AttemptData> attempts = new ConcurrentHashMap<>();

    public void registerFailure(String email, String ip) {
        String emailKey = "email:" + email;
        String ipKey    = "ip:" + ip;

        increment(emailKey);
        increment(ipKey);
    }

    public void registerSuccess(String email, String ip) {
        attempts.remove("email:" + email);
        attempts.remove("ip:" + ip);
    }

    public void checkBlocked(String email, String ip) {
        checkKey("email:" + email, "Your account");
        checkKey("ip:" + ip, "Your IP address");
    }

    private void increment(String key) {
        AttemptData data = attempts.computeIfAbsent(key, k -> new AttemptData());

        if (data.permanentlyBlocked) return;

        if (data.blockedUntil != null && data.blockedUntil.isBefore(LocalDateTime.now())) {
            data.blockedUntil = null;
        }

        data.totalAttempts++;
    }

    private void checkKey(String key, String subject) {
        AttemptData data = attempts.get(key);
        if (data == null) return;

        if (data.permanentlyBlocked) {
            throw new BlockedException(subject + " is permanently blocked. Please contact support.");
        }

        if (data.totalAttempts >= PERMANENT_BLOCK_ATTEMPTS) {
            data.permanentlyBlocked = true;
            throw new BlockedException(subject + " is permanently blocked. Please contact support.");
        }

        if (data.totalAttempts >= SECOND_BLOCK_ATTEMPTS) {
            applyTemporaryBlock(data, THIRD_BLOCK_MINUTES, subject);
            return;
        }

        if (data.totalAttempts >= FIRST_BLOCK_ATTEMPTS) {
            applyTemporaryBlock(data, SECOND_BLOCK_MINUTES, subject);
            return;
        }

        if (data.blockedUntil != null && data.blockedUntil.isAfter(LocalDateTime.now())) {
            long minutesLeft = java.time.Duration.between(LocalDateTime.now(), data.blockedUntil).toMinutes() + 1;
            throw new BlockedException(subject + " is blocked. Try again in " + minutesLeft + " minute(s).");
        }
    }

    private void applyTemporaryBlock(AttemptData data, int minutes, String subject) {
        if (data.blockedUntil == null || data.blockedUntil.isBefore(LocalDateTime.now())) {
            data.blockedUntil = LocalDateTime.now().plusMinutes(minutes);
        }
        long minutesLeft = java.time.Duration.between(LocalDateTime.now(), data.blockedUntil).toMinutes() + 1;
        throw new BlockedException(subject + " is blocked. Try again in " + minutesLeft + " minute(s).");
    }

    private static class AttemptData {
        int totalAttempts      = 0;
        boolean permanentlyBlocked = false;
        LocalDateTime blockedUntil = null;
    }
}