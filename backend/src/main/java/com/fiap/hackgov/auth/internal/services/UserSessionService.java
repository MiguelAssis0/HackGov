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
        var parsed = parseUserAgent(agent);
        UserSession session = new UserSession();
        session.setUser(user);
        session.setIpAddress(trim(ip, 80));
        session.setUserAgent(trim(agent, 500));
        session.setBrowser(parsed.browser());
        session.setBrowserVersion(parsed.browserVersion());
        session.setOperatingSystem(parsed.os());
        session.setDeviceType(parsed.deviceType());
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
        // ponytail: Django filtra UserSession.objects.filter(user=request.user) e delete no revoke → só ativos aparecem
        return repository.findByUser_IdOrderByLastActivityDesc(user.getId()).stream()
                .filter(s -> s.getRevokedAt() == null)
                .map(s -> new Response(
                        s.getId(), s.getId().toString(), deviceIcon(s.getDeviceType()), s.getBrowser(), s.getBrowserVersion(), s.getOperatingSystem(), s.getDeviceType(),
                        s.getIpAddress(), s.getUserAgent(), s.getCreatedAt(), s.getLastActivity(), s.getExpiresAt(), s.getRevokedAt(), s.getId().equals(current), s.active()
                )).toList();
    }

    @Transactional
    public void revoke(UUID id, User user, UUID current) {
        if (id.equals(current)) throw new BusinessException("A sessao atual deve ser encerrada pelo botao Sair");
        UserSession session = repository.findByIdAndUser_Id(id, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Sessao nao encontrada"));
        // Django 1:1: UserSession.objects.filter(session_key=...).delete() + Session.delete() → some da lista no reload
        repository.delete(session);
    }

    private String hash(String v) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // ponytail: 1:1 com Sistema-ERP-Municipal/accounts/middleware.py:_parse_user_agent
    private ParsedAgent parseUserAgent(String a) {
        if (a == null) a = "";
        String ua = a.toLowerCase(Locale.ROOT);
        String browser = "", browserVersion = "", os = "", deviceType = "desktop";
        // browser
        java.util.regex.Matcher m;
        String[][] browsers = {{"edg/([\\d.]+)", "Edge"}, {"opr/([\\d.]+)", "Opera"}, {"chrome/([\\d.]+)", "Chrome"}, {"firefox/([\\d.]+)", "Firefox"}, {"safari/([\\d.]+)", "Safari"}};
        for (String[] b : browsers) {
            m = java.util.regex.Pattern.compile(b[0]).matcher(ua);
            if (m.find()) { browser = b[1]; browserVersion = m.group(1); break; }
        }
        // os
        String[][] oses = {{"windows nt ([\\d.]+)", "Windows"}, {"mac os x ([\\d_]+)", "macOS"}, {"android ([\\d.]+)", "Android"}, {"linux", "Linux"}, {"iphone.*os ([\\d_]+)", "iOS"}, {"ipad.*os ([\\d_]+)", "iPadOS"}, {"cros", "ChromeOS"}};
        for (String[] o : oses) {
            if (java.util.regex.Pattern.compile(o[0]).matcher(ua).find()) { os = o[1]; break; }
        }
        if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("android") && ua.contains("mobile")) deviceType = "mobile";
        else if (ua.contains("tablet") || ua.contains("ipad") || (ua.contains("android") && !ua.contains("mobile"))) deviceType = "tablet";
        return new ParsedAgent(browser, browserVersion, os, deviceType);
    }

    private record ParsedAgent(String browser, String browserVersion, String os, String deviceType) {}

    private String deviceIcon(String deviceType) {
        return switch (deviceType) {
            case "mobile" -> "bi-phone";
            case "tablet" -> "bi-tablet";
            default -> "bi-laptop";
        };
    }

    private String trim(String v, int n) {
        if (v == null) return "";
        return v.length() > n ? v.substring(0, n) : v;
    }

    public record Tokens(String accessToken, String refreshToken, UUID sessionId) {
    }

    public record Response(
            UUID id, String sessionKey, String deviceIcon,
            String browser, String browserVersion, String operatingSystem, String deviceType,
            String ipAddress, String userAgent, LocalDateTime createdAt, LocalDateTime lastActivity,
            LocalDateTime expiresAt, LocalDateTime revokedAt, boolean current, boolean active
    ) {
        // Django 1:1 aliases for perfil.html
        public String session_key() { return sessionKey; }
        public String device_icon() { return deviceIcon; }
        public String browser_version() { return browserVersion; }
        public String user_agent() { return userAgent; }
        public String ip_address() { return ipAddress; }
        public LocalDateTime last_activity() { return lastActivity; }
        public String os() { return operatingSystem; }
    }
}
