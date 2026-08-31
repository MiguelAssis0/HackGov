package com.fiap.hackgov.auth.internal.services;

import com.fiap.hackgov.auth.internal.DTOs.LoginRequestDTO;
import com.fiap.hackgov.auth.internal.DTOs.LoginResponseDTO;
import com.fiap.hackgov.auth.internal.DTOs.RefreshToken.RefreshTokenRequestDTO;
import com.fiap.hackgov.auth.internal.DTOs.RefreshToken.RefreshTokenResponseDTO;
import com.fiap.hackgov.auth.internal.DTOs.TwoFactorRequestDTO;
import com.fiap.hackgov.auth.internal.DTOs.TwoFactorResponseDTO;
import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.auth.internal.repositories.UserRepository;
import com.fiap.hackgov.shared.infra.exceptions.InvalidCredentialsException;
import com.fiap.hackgov.shared.infra.services.LoginAttemptService;
import com.fiap.hackgov.shared.infra.services.TokenBlacklistService;
import com.fiap.hackgov.shared.infra.services.TokenService;
import com.fiap.hackgov.shared.infra.utils.AuditLog;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private AuditLog auditLog;

    @Autowired
    private UserSessionService userSessionService;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private String dummyPasswordHash;

    @PostConstruct
    void initializeDummyPasswordHash() {
        dummyPasswordHash = passwordEncoder.encode("hackgov-invalid-login-dummy-password");
    }

    public void logout(String token) {
        if (token == null) {
            auditLog.with(log).event("logout_failed").level(AuditLog.Level.ERROR).log();
            throw new InvalidCredentialsException("Invalid token");
        }


        LocalDateTime expiration = tokenService.getExpirationAsLocalDateTime(token);
        userSessionService.revokeCurrent(token);

        auditLog.with(log).event("logout").level(AuditLog.Level.INFO).log();

        tokenBlacklistService.blacklist(token, expiration);
    }

    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        auditLog.with(log).event("refresh_token_attempt").level(AuditLog.Level.INFO).log();

        String email = tokenService.getSubjectFromRefreshToken(request.refreshToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    auditLog.with(log).event("refresh_token_failed").email(email).reason("user_not_found").level(AuditLog.Level.ERROR).log();
                    return new InvalidCredentialsException("Invalid credentials");
                });

        if (!user.getStatus()) {
            auditLog.with(log).event("refresh_token_failed").email(email).reason("inactive_account").level(AuditLog.Level.ERROR).log();
            throw new InvalidCredentialsException("Invalid credentials");
        }

        auditLog.with(log).event("refresh_token_success").email(email).level(AuditLog.Level.INFO).log();

        var issued = userSessionService.rotate(request.refreshToken(), user);
        String newAccessToken = issued.accessToken();
        String newRefreshToken = issued.refreshToken();

        return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest, String clientIp, String userAgent) {
        auditLog.with(log).event("login_attempt").email(loginRequest.email()).level(AuditLog.Level.INFO).log();

        loginAttemptService.checkBlocked(clientIp);

        Optional<User> userOpt = userRepository.findByEmail(loginRequest.email());

        String passwordHash = userOpt.map(User::getPassword).orElse(dummyPasswordHash);
        boolean passwordMatches = safelyMatches(loginRequest.password(), passwordHash);

        if (userOpt.isEmpty() || !passwordMatches) {
            auditLog.with(log)
                    .event("login_failed")
                    .email(loginRequest.email())
                    .reason("invalid_credentials")
                    .level(AuditLog.Level.ERROR)
                    .log();
            loginAttemptService.registerFailure(clientIp);
            throw new InvalidCredentialsException("Invalid credentials");
        }

        User user = userOpt.get();

        if (!user.getStatus()) {
            auditLog.with(log)
                    .event("login_failed")
                    .email(loginRequest.email())
                    .reason("inactive_account")
                    .level(AuditLog.Level.ERROR)
                    .log();
            throw new InvalidCredentialsException("Account is inactive");
        }

        loginAttemptService.registerSuccess(clientIp);

        if (user.getTwoFactor()) {
            auditLog.with(log).event("2fa_sent").email(user.getEmail()).level(AuditLog.Level.INFO).log();
            twoFactorAuthService.sendTwoFactorCode(user.getEmail(), user.getFullName());
            return new LoginResponseDTO(null, null, true);
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        var issued = userSessionService.issue(user, clientIp, userAgent);
        String accessToken = issued.accessToken();
        String refreshToken = issued.refreshToken();

        auditLog.with(log).event("login_success").email(user.getEmail()).level(AuditLog.Level.INFO).log();

        return new LoginResponseDTO(accessToken, refreshToken, false);
    }

    private boolean safelyMatches(String rawPassword, String encodedPassword) {
        try {
            return encodedPassword != null && passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            log.warn("Ignoring invalid stored password hash during login");
            return false;
        }
    }

    public TwoFactorResponseDTO verifyTwoFactor(TwoFactorRequestDTO twoFactorRequest, String clientIp, String userAgent) {
        loginAttemptService.checkTwoFactorBlocked(clientIp);
        Optional<User> userOpt = userRepository.findByEmail(twoFactorRequest.email());


        if (userOpt.isEmpty()) {
            auditLog.with(log).event("2fa_verify_failed").email(twoFactorRequest.email()).level(AuditLog.Level.WARN).log();
            throw new InvalidCredentialsException("Invalid credentials");
        }

        User user = userOpt.get();

        if (!user.getTwoFactor()) {
            auditLog.with(log).event("2fa_verify_failed").email(twoFactorRequest.email()).level(AuditLog.Level.WARN).log();
            throw new InvalidCredentialsException("Two-factor authentication not enabled for this account");
        }


        boolean isValid = twoFactorAuthService.verifyCode(twoFactorRequest.email(), twoFactorRequest.code());

        if (!isValid) {
            auditLog.with(log).event("2fa_verify_failed").email(twoFactorRequest.email()).level(AuditLog.Level.WARN).log();
            loginAttemptService.registerTwoFactorFailure(clientIp);
            throw new InvalidCredentialsException("Invalid two-factor code");
        }

        loginAttemptService.registerTwoFactorSuccess(clientIp);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        var issued = userSessionService.issue(user, clientIp, userAgent);
        String accessToken = issued.accessToken();
        String refreshToken = issued.refreshToken();

        auditLog.with(log).event("2fa_verify_success").email(twoFactorRequest.email()).level(AuditLog.Level.INFO).log();

        return new TwoFactorResponseDTO(accessToken, refreshToken, "Two-factor authentication successful");
    }
}
