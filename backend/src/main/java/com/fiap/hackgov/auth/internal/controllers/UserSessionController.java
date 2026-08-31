package com.fiap.hackgov.auth.internal.controllers;

import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.auth.internal.services.UserSessionService;
import com.fiap.hackgov.shared.infra.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class UserSessionController {
    private final UserSessionService service;
    private final TokenService tokens;

    @GetMapping
    public List<UserSessionService.Response> list(@AuthenticationPrincipal User user, HttpServletRequest request) {
        return service.list(user, tokens.getSessionId(tokens.extractToken(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id, @AuthenticationPrincipal User user, HttpServletRequest request) {
        service.revoke(id, user, tokens.getSessionId(tokens.extractToken(request)));
        return ResponseEntity.noContent().build();
    }
}
