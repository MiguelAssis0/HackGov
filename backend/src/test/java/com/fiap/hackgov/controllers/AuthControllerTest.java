package com.fiap.hackgov.controllers;

import com.fiap.hackgov.auth.internal.DTOs.LoginRequestDTO;
import com.fiap.hackgov.auth.internal.DTOs.LoginResponseDTO;
import com.fiap.hackgov.auth.internal.DTOs.TwoFactorRequestDTO;
import com.fiap.hackgov.auth.internal.DTOs.TwoFactorResponseDTO;
import com.fiap.hackgov.auth.internal.controllers.AuthController;
import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.shared.infra.filters.JwtAuthenticationFilter;
import com.fiap.hackgov.shared.infra.filters.RateLimitFilter;
import com.fiap.hackgov.shared.infra.security.Security;
import com.fiap.hackgov.shared.infra.security.SecurityProperties;
import com.fiap.hackgov.shared.infra.services.TokenService;
import com.fiap.hackgov.auth.internal.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                Security.class,
                                JwtAuthenticationFilter.class,
                                RateLimitFilter.class
                        }
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private SecurityProperties securityProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Login sem 2FA deve retornar token e requiresTwoFactor false")
    void login_Success_Without2FA() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password123");
        LoginResponseDTO loginResponse = new LoginResponseDTO(
                "access-token-mock",
                "refresh-token-mock",
                "test@example.com",
                "Test User",
                Roles.EMPLOYEE,
                false  // sem 2FA
        );
        when(authService.login(any(LoginRequestDTO.class), any(String.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("access-token-mock"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-mock"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.requiresTwoFactor").value(false));
    }

    @Test
    @DisplayName("Login com 2FA deve retornar tokens nulos e requiresTwoFactor true")
    void login_Success_With2FA() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password123");
        LoginResponseDTO loginResponse = new LoginResponseDTO(
                null,
                null,
                "test@example.com",
                "Test User",
                Roles.EMPLOYEE,
                true  // com 2FA
        );
        when(authService.login(any(LoginRequestDTO.class), any(String.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").isEmpty())
                .andExpect(jsonPath("$.refreshToken").isEmpty())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.requiresTwoFactor").value(true));
    }

    @Test
    @DisplayName("Login com email vazio deve retornar 400")
    void login_InvalidInput_ReturnsBadRequest() throws Exception {
        LoginRequestDTO invalidRequest = new LoginRequestDTO("", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Verificação de 2FA com código válido deve retornar tokens")
    void verifyTwoFactor_Success() throws Exception {
        TwoFactorRequestDTO twoFactorRequest = new TwoFactorRequestDTO("test@example.com", "123456");
        TwoFactorResponseDTO twoFactorResponse = new TwoFactorResponseDTO(
                "access-token-mock",
                "refresh-token-mock",
                "Two-factor authentication successful"
        );
        when(authService.verifyTwoFactor(any(TwoFactorRequestDTO.class), any(String.class)))
                .thenReturn(twoFactorResponse);

        mockMvc.perform(post("/api/auth/2fa/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(twoFactorRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("access-token-mock"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-mock"))
                .andExpect(jsonPath("$.message").value("Two-factor authentication successful"));
    }

    @Test
    @DisplayName("Verificação de 2FA com email vazio deve retornar 400")
    void verifyTwoFactor_InvalidInput_ReturnsBadRequest() throws Exception {
        TwoFactorRequestDTO invalidRequest = new TwoFactorRequestDTO("", "123456");

        mockMvc.perform(post("/api/auth/2fa/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}