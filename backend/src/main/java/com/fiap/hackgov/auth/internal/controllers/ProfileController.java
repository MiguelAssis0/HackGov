package com.fiap.hackgov.auth.internal.controllers;

import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.auth.internal.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @PutMapping("/two-factor")
    public ResponseEntity<Map<String, Object>> toggleTwoFactor(@AuthenticationPrincipal User principal, @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) enabled = body.get("twoFactorAuth");
        User user = userRepository.findById(principal.getId()).orElseThrow();
        user.setTwoFactor(Boolean.TRUE.equals(enabled));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("ok", true, "two_factor_auth", user.getTwoFactor()));
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(@AuthenticationPrincipal User principal, @RequestBody Map<String, Object> body) {
        User user = userRepository.findById(principal.getId()).orElseThrow();
        if (body.containsKey("modo_escuro")) user.setDarkMode(Boolean.TRUE.equals(body.get("modo_escuro")));
        if (body.containsKey("darkMode")) user.setDarkMode(Boolean.TRUE.equals(body.get("darkMode")));
        if (body.containsKey("notificacoes")) user.setNotifications(Boolean.TRUE.equals(body.get("notificacoes")));
        if (body.containsKey("notifications")) user.setNotifications(Boolean.TRUE.equals(body.get("notifications")));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/accessibility")
    public ResponseEntity<Map<String, Object>> updateAccessibility(@AuthenticationPrincipal User principal, @RequestBody Map<String, Object> body) {
        User user = userRepository.findById(principal.getId()).orElseThrow();
        if (body.containsKey("vlibras")) user.setAccessibility(Boolean.TRUE.equals(body.get("vlibras")));
        if (body.containsKey("tamanho_fonte")) user.setFontSize(String.valueOf(body.get("tamanho_fonte")));
        if (body.containsKey("fontSize")) user.setFontSize(String.valueOf(body.get("fontSize")));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings(@AuthenticationPrincipal User principal) {
        User user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "modo_escuro", Boolean.TRUE.equals(user.getDarkMode()),
                "darkMode", Boolean.TRUE.equals(user.getDarkMode()),
                "notificacoes", user.getNotifications() == null || user.getNotifications(),
                "notifications", user.getNotifications() == null || user.getNotifications(),
                "vlibras", Boolean.TRUE.equals(user.getAccessibility()),
                "tamanho_fonte", user.getFontSize() == null ? "medio" : user.getFontSize(),
                "fontSize", user.getFontSize() == null ? "medio" : user.getFontSize(),
                "two_factor_auth", Boolean.TRUE.equals(user.getTwoFactor())
        ));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(@AuthenticationPrincipal User principal, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(principal.getId()).orElseThrow();
        if (body.containsKey("nome") && !body.get("nome").isBlank()) {
            String[] parts = body.get("nome").trim().split("\\s+", 2);
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1 ? parts[1] : "");
        }
        if (body.containsKey("email") && !body.get("email").isBlank()) user.setEmail(body.get("email").trim().toLowerCase());
        if (body.containsKey("cpf") && !body.get("cpf").isBlank()) user.setCpf(body.get("cpf").replaceAll("\\D", ""));
        if (body.containsKey("celular") && !body.get("celular").isBlank()) user.setPhone(body.get("celular").replaceAll("\\D", ""));
        if (body.containsKey("avatar") && body.get("avatar") != null) user.setAvatarPath(body.get("avatar"));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
