package com.fiap.hackgov.auth.internal.services;

import com.fiap.hackgov.auth.internal.DTOs.LoginRequestDTO;
import com.fiap.hackgov.shared.infra.exceptions.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("dev")
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void unknownUserReturnsInvalidCredentialsInsteadOfArgon2Failure() {
        LoginRequestDTO request = new LoginRequestDTO(
                "missing-" + UUID.randomUUID() + "@example.invalid",
                "irrelevant-password"
        );

        assertThatThrownBy(() -> authService.login(request, "198.51.100.10", "integration-test"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}
