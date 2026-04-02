package com.fiap.hackgov.infra.security;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class EncryptPassword {

    private final Argon2PasswordEncoder encoder =
            Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public String encrypt(String password) {
        return encoder.encode(password);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}