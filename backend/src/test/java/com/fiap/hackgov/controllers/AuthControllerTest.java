package com.fiap.hackgov.controllers;

import com.fiap.hackgov.DTOs.Auth.LoginRequestDTO;
import com.fiap.hackgov.DTOs.Auth.LoginResponseDTO;
import com.fiap.hackgov.DTOs.Auth.TwoFactorRequestDTO;
import com.fiap.hackgov.DTOs.Auth.TwoFactorResponseDTO;
import com.fiap.hackgov.entities.enums.Roles;
import com.fiap.hackgov.infra.filters.Filter;
import com.fiap.hackgov.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.security.SecurityConfig;
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
                                SecurityConfig.class,
                                Filter.class
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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_Success_Without2FA() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password123");
        LoginResponseDTO loginResponse = new LoginResponseDTO("jwt-token", "test@example.com", "Test User", Roles.EMPLOYEE, false);

        when(authService.login(any(LoginRequestDTO.class), any(String.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.requiresTwoFactor").value(false));
    }

    @Test
    void login_Success_With2FA() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password123");
        LoginResponseDTO loginResponse = new LoginResponseDTO(null, "test@example.com", "Test User", Roles.EMPLOYEE, true);

        when(authService.login(any(LoginRequestDTO.class), any(String.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isEmpty())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.requiresTwoFactor").value(true));
    }

    @Test
    void login_InvalidInput_ReturnsBadRequest() throws Exception {
        LoginRequestDTO invalidRequest = new LoginRequestDTO("", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyTwoFactor_Success() throws Exception {
        TwoFactorRequestDTO twoFactorRequest = new TwoFactorRequestDTO("test@example.com", "123456");
        TwoFactorResponseDTO twoFactorResponse = new TwoFactorResponseDTO("jwt-token", "Two-factor authentication successful");

        when(authService.verifyTwoFactor(any(TwoFactorRequestDTO.class))).thenReturn(twoFactorResponse);

        mockMvc.perform(post("/api/auth/2fa/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(twoFactorRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.message").value("Two-factor authentication successful"));
    }

    @Test
    void verifyTwoFactor_InvalidInput_ReturnsBadRequest() throws Exception {
        TwoFactorRequestDTO invalidRequest = new TwoFactorRequestDTO("", "123456");

        mockMvc.perform(post("/api/auth/2fa/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
