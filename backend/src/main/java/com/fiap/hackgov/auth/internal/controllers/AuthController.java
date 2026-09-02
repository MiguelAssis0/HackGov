package com.fiap.hackgov.auth.internal.controllers;

import com.fiap.hackgov.auth.internal.DTOs.LoginRequestDTO;
import com.fiap.hackgov.auth.internal.DTOs.LoginResponseDTO;
import com.fiap.hackgov.auth.internal.DTOs.RefreshToken.RefreshTokenRequestDTO;
import com.fiap.hackgov.auth.internal.DTOs.RefreshToken.RefreshTokenResponseDTO;
import com.fiap.hackgov.auth.internal.DTOs.TwoFactorRequestDTO;
import com.fiap.hackgov.auth.internal.DTOs.TwoFactorResponseDTO;
import com.fiap.hackgov.auth.internal.services.AuthService;
import com.fiap.hackgov.shared.infra.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

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
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        LoginResponseDTO response = authService.login(loginRequest, clientIp, httpRequest.getHeader("User-Agent"));
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
    public ResponseEntity<TwoFactorResponseDTO> verifyTwoFactor(
            @RequestBody @Valid TwoFactorRequestDTO twoFactorRequest,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        return ResponseEntity.ok(authService.verifyTwoFactor(twoFactorRequest, clientIp, httpRequest.getHeader("User-Agent")));
    }

    public record ResendRequest(@jakarta.validation.constraints.Email @jakarta.validation.constraints.NotBlank String email) {}

    @Operation(summary = "Resend Two-Factor Code", description = "Resend 2FA code via email (Mailpit in dev, SMTP in prod)")
    @PostMapping("/2fa/resend")
    public ResponseEntity<Void> resendTwoFactor(@RequestBody @Valid ResendRequest request) {
        authService.resendTwoFactorCode(request.email());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Logout", description = "Invalidate current access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = tokenService.extractToken(request);
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Refresh Token", description = "Generate new access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successful"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
