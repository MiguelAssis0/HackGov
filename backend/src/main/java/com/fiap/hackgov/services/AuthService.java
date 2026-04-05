package com.fiap.hackgov.services;

import com.fiap.hackgov.DTOs.Auth.LoginRequestDTO;
import com.fiap.hackgov.DTOs.Auth.LoginResponseDTO;
import com.fiap.hackgov.DTOs.Auth.RefreshToken.RefreshTokenRequestDTO;
import com.fiap.hackgov.DTOs.Auth.RefreshToken.RefreshTokenResponseDTO;
import com.fiap.hackgov.DTOs.Auth.TwoFactorRequestDTO;
import com.fiap.hackgov.DTOs.Auth.TwoFactorResponseDTO;
import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.infra.exceptions.InvalidCredentialsException;
import com.fiap.hackgov.infra.security.TokenService;
import com.fiap.hackgov.infra.utils.AuditLog;
import com.fiap.hackgov.repositories.EmployeeRepository;
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
    private EmployeeRepository employeeRepository;

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

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    public void logout(String token){
        if(token == null){
            auditLog.with(log).event("logout_failed").level(AuditLog.Level.ERROR).log();
            throw new InvalidCredentialsException("Invalid token");
        }


        LocalDateTime expiration = tokenService.getExpirationAsLocalDateTime(token);

        auditLog.with(log).event("logout").level(AuditLog.Level.INFO).log();

        tokenBlacklistService.blacklist(token, expiration);
    }

    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        auditLog.with(log).event("refresh_token_attempt").level(AuditLog.Level.INFO).log();

        String email = tokenService.getSubjectFromRefreshToken(request.refreshToken());

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    auditLog.with(log).event("refresh_token_failed").email(email).reason("user_not_found").level(AuditLog.Level.ERROR).log();
                    return new InvalidCredentialsException("Invalid credentials");
                });

        if (!employee.isStatus()) {
            auditLog.with(log).event("refresh_token_failed").email(email).reason("inactive_account").level(AuditLog.Level.ERROR).log();
            throw new InvalidCredentialsException("Invalid credentials");
        }

        auditLog.with(log).event("refresh_token_success").email(email).level(AuditLog.Level.INFO).log();

        String newAccessToken  = tokenService.generateToken(employee);
        String newRefreshToken = tokenService.generateRefreshToken(employee);

        return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest, String clientIp) {
        auditLog.with(log).event("login_attempt").email(loginRequest.email()).level(AuditLog.Level.INFO).log();

        loginAttemptService.checkBlocked(clientIp);

        Optional<Employee> employeeOpt = employeeRepository.findByEmail(loginRequest.email());

        String dummyHash = "$argon2id$v=19$m=16384,t=2,p=1$abc$def";
        String passwordHash = employeeOpt.map(Employee::getPassword).orElse(dummyHash);
        boolean passwordMatches = passwordEncoder.matches(loginRequest.password(), passwordHash);

        if (employeeOpt.isEmpty() || !passwordMatches) {
            auditLog.with(log)
                    .event("login_failed")
                    .email(loginRequest.email())
                    .reason("invalid_credentials")
                    .level(AuditLog.Level.ERROR)
                    .log();
            loginAttemptService.registerFailure(clientIp);
            throw new InvalidCredentialsException("Invalid credentials");
        }

        Employee employee = employeeOpt.get();

        if (!employee.isStatus()) {
            auditLog.with(log)
                    .event("login_failed")
                    .email(loginRequest.email())
                    .reason("inactive_account")
                    .level(AuditLog.Level.ERROR)
                    .log();
            throw new InvalidCredentialsException("Account is inactive");
        }

        loginAttemptService.registerSuccess(clientIp);

        if (employee.isTwoFactor()) {
            auditLog.with(log).event("2fa_sent").email(employee.getEmail()).level(AuditLog.Level.INFO).log();
            twoFactorAuthService.sendTwoFactorCode(employee.getEmail(), employee.getName());
            return new LoginResponseDTO(null, null, employee.getEmail(), employee.getName(), employee.getRole(), true);
        }

        employee.setLastLogin(LocalDateTime.now());
        employeeRepository.save(employee);

        String accessToken  = tokenService.generateToken(employee);
        String refreshToken = tokenService.generateRefreshToken(employee);

        auditLog.with(log).event("login_success").email(employee.getEmail()).level(AuditLog.Level.INFO).log();

        return new LoginResponseDTO(accessToken, refreshToken, employee.getEmail(), employee.getName(), employee.getRole(), false);
    }

    public TwoFactorResponseDTO verifyTwoFactor(TwoFactorRequestDTO twoFactorRequest, String clientIp) {
        loginAttemptService.checkTwoFactorBlocked(clientIp);
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(twoFactorRequest.email());


        if (employeeOpt.isEmpty()) {
            auditLog.with(log).event("2fa_verify_failed").email(twoFactorRequest.email()).level(AuditLog.Level.WARN).log();
            throw new InvalidCredentialsException("Invalid credentials");
        }

        Employee employee = employeeOpt.get();

        if (!employee.isTwoFactor()) {
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

        employee.setLastLogin(LocalDateTime.now());
        employeeRepository.save(employee);

        String accessToken = tokenService.generateToken(employee);
        String refreshToken = tokenService.generateRefreshToken(employee);

        auditLog.with(log).event("2fa_verify_success").email(twoFactorRequest.email()).level(AuditLog.Level.INFO).log();

        return new TwoFactorResponseDTO(accessToken, refreshToken, "Two-factor authentication successful");
    }
}