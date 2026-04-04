package com.fiap.hackgov.controllers;

import com.fiap.hackgov.DTOs.Auth.LoginRequestDTO;
import com.fiap.hackgov.DTOs.Auth.LoginResponseDTO;
import com.fiap.hackgov.DTOs.Auth.TwoFactorRequestDTO;
import com.fiap.hackgov.DTOs.Auth.TwoFactorResponseDTO;
import com.fiap.hackgov.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.bouncycastle.util.IPAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Employee Login", description = "Authenticate employee with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "429", description = "Too many attempts - blocked"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO loginRequest,
            HttpServletRequest httpRequest) { // CORRIGIDO

        String clientIp = getClientIp(httpRequest); // extrai o IP aqui
        LoginResponseDTO response = authService.login(loginRequest, clientIp);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Operation(summary = "Verify Two-Factor Authentication", description = "Verify 2FA code for employee login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "2FA verification successful"),
            @ApiResponse(responseCode = "401", description = "Invalid 2FA code"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/2fa/verify")
    public ResponseEntity<TwoFactorResponseDTO> verifyTwoFactor(@RequestBody @Valid TwoFactorRequestDTO twoFactorRequest) {
        TwoFactorResponseDTO response = authService.verifyTwoFactor(twoFactorRequest);
        return ResponseEntity.ok(response);
    }
}
