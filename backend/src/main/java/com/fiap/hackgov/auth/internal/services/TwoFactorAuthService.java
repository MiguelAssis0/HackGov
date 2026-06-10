package com.fiap.hackgov.auth.internal.services;

import com.fiap.hackgov.auth.internal.entities.TwoFactorCode;
import com.fiap.hackgov.auth.internal.repositories.TwoFactorCodeRepository;
import com.fiap.hackgov.shared.infra.utils.AuditLog;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TwoFactorAuthService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TwoFactorCodeRepository twoFactorCodeRepository;

    @Autowired
    private AuditLog auditLog;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final Logger log = LoggerFactory.getLogger(TwoFactorAuthService.class);


    public String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }

    public void sendTwoFactorCode(String email, String name) {
        auditLog.with(log).event("2fa_generate_start").email(email).level(AuditLog.Level.INFO).log();

        String code = generateCode();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(10);

        TwoFactorCode twoFactorCode = twoFactorCodeRepository.findByEmail(email)
                .orElse(new TwoFactorCode());
        twoFactorCode.setEmail(email);
        twoFactorCode.setCode(code);
        twoFactorCode.setExpiration(expiration);
        twoFactorCodeRepository.save(twoFactorCode);

        auditLog.with(log).event("2fa_code_persisted").email(email).level(AuditLog.Level.INFO).log();

        try {
            SimpleMailMessage message = getSimpleMailMessage(email, name, code);
            mailSender.send(message);

            auditLog.with(log).event("2fa_email_sent").email(email).level(AuditLog.Level.INFO).log();
        } catch (Exception e) {
            auditLog.with(log).event("2fa_email_failed").reason(e.getMessage()).email(email).level(AuditLog.Level.ERROR).log();
            twoFactorCodeRepository.deleteByEmail(email);
            throw new RuntimeException("Failed to send two-factor authentication code", e);
        }
    }

    private static @NonNull SimpleMailMessage getSimpleMailMessage(String email, String name, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("HackGov - Two-Factor Authentication Code");
        message.setText("Hello " + name + ",\n\n" +
                "Your two-factor authentication code is: " + code + "\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you didn't request this code, please ignore this email.\n\n" +
                "Best regards,\n" +
                "HackGov Team");
        return message;
    }

    public boolean verifyCode(String email, String code) {
        auditLog.with(log).event("2fa_verify_start").email(email).level(AuditLog.Level.INFO).log();
        Optional<TwoFactorCode> storedCodeOpt = twoFactorCodeRepository.findByEmail(email);

        if (storedCodeOpt.isEmpty()) {
            auditLog.with(log).event("2fa_verify_failed").reason("code_not_found").email(email).level(AuditLog.Level.WARN).log();
            return false;
        }

        TwoFactorCode storedCode = storedCodeOpt.get();

        if (storedCode.getExpiration().isBefore(LocalDateTime.now())) {
            auditLog.with(log).event("2fa_verify_failed").reason("code_expired").email(email).level(AuditLog.Level.WARN).log();
            twoFactorCodeRepository.deleteByEmail(email);
            return false;
        }

        boolean isValid = MessageDigest.isEqual(
                storedCode.getCode().getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8)
        );

        if (isValid) {
            auditLog.with(log).reason("2fa_verify_success").email(email).level(AuditLog.Level.INFO).log();
            twoFactorCodeRepository.deleteByEmail(email);
        } else {
            auditLog.with(log).reason("2fa_verify_failed").reason("invalid_code").email(email).level(AuditLog.Level.WARN).log();
        }

        return isValid;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanExpiredCodes() {
        auditLog.with(log).event("2fa_cleanup_start").level(AuditLog.Level.INFO).log();
        twoFactorCodeRepository.deleteAllExpired(LocalDateTime.now());
        auditLog.with(log).event("2fa_cleanup_done").level(AuditLog.Level.INFO).log();
    }
}