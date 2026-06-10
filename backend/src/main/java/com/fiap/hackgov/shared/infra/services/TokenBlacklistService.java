package com.fiap.hackgov.shared.infra.services;

import com.fiap.hackgov.shared.infra.entities.BlacklistedToken;
import com.fiap.hackgov.shared.infra.repositories.BlacklistedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenBlacklistService {

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    public void blacklist(String token, LocalDateTime expiresAt) {
        blacklistedTokenRepository.save(new BlacklistedToken(token, expiresAt));
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanExpiredTokens() {
        blacklistedTokenRepository.deleteAllExpired(LocalDateTime.now());
    }
}