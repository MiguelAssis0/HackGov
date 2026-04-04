package com.fiap.hackgov.services;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TwoFactorAuthService {

    @Autowired
    private JavaMailSender mailSender;

    private final ConcurrentMap<String, TwoFactorCode> codeStorage = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }

    public void sendTwoFactorCode(String email, String name) {
        String code = generateCode();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(10);

        codeStorage.put(email, new TwoFactorCode(code, expiration));

        try {
            SimpleMailMessage message = getSimpleMailMessage(email, name, code);
            mailSender.send(message);
        } catch (Exception e) {
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
        TwoFactorCode storedCode = codeStorage.get(email);

        if (storedCode == null) {
            return false;
        }

        if (storedCode.expiration.isBefore(LocalDateTime.now())) {
            codeStorage.remove(email);
            return false;
        }

        boolean isValid = MessageDigest.isEqual(
                storedCode.code.getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8)
        );

        if (isValid) {
            codeStorage.remove(email);
        }

        return isValid;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanExpiredCodes() {
        codeStorage.entrySet().removeIf(e ->
                e.getValue().expiration.isBefore(LocalDateTime.now())
        );
    }

    private record TwoFactorCode(String code, LocalDateTime expiration) {
    }
}