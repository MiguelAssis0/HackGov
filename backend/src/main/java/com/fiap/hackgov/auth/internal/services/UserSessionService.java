package com.fiap.hackgov.auth.internal.services;

import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.auth.internal.entities.UserSession;
import com.fiap.hackgov.auth.internal.repositories.UserSessionRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.InvalidCredentialsException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.TokenInvalidException;
import com.fiap.hackgov.shared.infra.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSessionService {
    private final UserSessionRepository repository;
    private final TokenService tokenService;

    @Transactional
    public Tokens issue(User user, String ip, String agent) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setIpAddress(trim(ip, 80));
        session.setUserAgent(trim(agent, 500));
        session.setBrowser(browser(agent));
        session.setOperatingSystem(os(agent));
        session.setDeviceType(device(agent));
        session.setLastActivity(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session = repository.save(session);
        String access = tokenService.generateToken(user, session.getId()), refresh = tokenService.generateRefreshToken(user, session.getId());
        session.setRefreshTokenHash(hash(refresh));
        repository.save(session);
        return new Tokens(access, refresh, session.getId());
    }

    @Transactional
    public Tokens rotate(String refresh, User user) {
        UUID id = tokenService.getSessionIdFromRefreshToken(refresh);
        UserSession session = repository.findByIdAndUser_Id(id, user.getId()).filter(UserSession::active).orElseThrow(() -> new InvalidCredentialsException("Sessao revogada ou expirada"));
        if (!MessageDigest.isEqual(hash(refresh).getBytes(StandardCharsets.UTF_8), session.getRefreshTokenHash().getBytes(StandardCharsets.UTF_8)))
            throw new InvalidCredentialsException("Refresh token invalido");
        String access = tokenService.generateToken(user, id), next = tokenService.generateRefreshToken(user, id);
        session.setRefreshTokenHash(hash(next));
        session.setLastActivity(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        repository.save(session);
        return new Tokens(access, next, id);
    }

    @Transactional
    public void validateAndTouch(UUID id, User user) {
        UserSession session = repository.findByIdAndUser_Id(id, user.getId()).filter(UserSession::active).orElseThrow(() -> new TokenInvalidException("Sessao revogada ou expirada"));
        if (session.getLastActivity() == null || session.getLastActivity().isBefore(LocalDateTime.now().minusMinutes(1))) {
            session.setLastActivity(LocalDateTime.now());
            repository.save(session);
        }
    }

    @Transactional
    public void revokeCurrent(String token) {
        UUID id = tokenService.getSessionId(token);
        repository.findById(id).ifPresent(s -> {
            s.setRevokedAt(LocalDateTime.now());
            repository.save(s);
        });
    }

    @Transactional(readOnly = true)
    public List<Response> list(User user, UUID current) {
        return repository.findByUser_IdOrderByLastActivityDesc(user.getId()).stream().map(s -> new Response(s.getId(), s.getBrowser(), s.getOperatingSystem(), s.getDeviceType(), s.getIpAddress(), s.getUserAgent(), s.getCreatedAt(), s.getLastActivity(), s.getExpiresAt(), s.getRevokedAt(), s.getId().equals(current), s.active())).toList();
    }

    @Transactional
    public void revoke(UUID id, User user, UUID current) {
        if (id.equals(current)) throw new BusinessException("A sessao atual deve ser encerrada pelo botao Sair");
        UserSession session = repository.findByIdAndUser_Id(id, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Sessao nao encontrada"));
        session.setRevokedAt(LocalDateTime.now());
        repository.save(session);
    }

    private String hash(String v) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String browser(String a) {
        String v = a == null ? "" : a;
        return v.contains("Firefox") ? "Firefox" : v.contains("Edg/") ? "Edge" : v.contains("Chrome") ? "Chrome" : v.contains("Safari") ? "Safari" : "Navegador";
    }

    private String os(String a) {
        String v = a == null ? "" : a;
        return v.contains("Windows") ? "Windows" : v.contains("Android") ? "Android" : v.contains("iPhone") || v.contains("iPad") ? "iOS" : v.contains("Linux") ? "Linux" : v.contains("Mac OS") ? "macOS" : "";
    }

    private String device(String a) {
        String v = a == null ? "" : a.toLowerCase(Locale.ROOT);
        return v.contains("mobile") || v.contains("iphone") ? "mobile" : v.contains("tablet") || v.contains("ipad") ? "tablet" : "desktop";
    }

    private String trim(String v, int n) {
        if (v == null) return "";
        return v.length() > n ? v.substring(0, n) : v;
    }

    public record Tokens(String accessToken, String refreshToken, UUID sessionId) {
    }

    public record Response(UUID id, String browser, String operatingSystem, String deviceType, String ipAddress,
                           String userAgent, LocalDateTime createdAt, LocalDateTime lastActivity,
                           LocalDateTime expiresAt, LocalDateTime revokedAt, boolean current, boolean active) {
    }
}
