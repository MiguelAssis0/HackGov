package com.fiap.hackgov.cityhall_management.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.CreationToken;
import com.fiap.hackgov.cityhall_management.internal.services.CreationTokenService;
import com.fiap.hackgov.shared.infra.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/tokens")
public class CreationTokenController {

    @Autowired
    private CreationTokenService creationTokenService;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<CreationToken> generateToken(HttpServletRequest request) {
        String token = tokenService.extractToken(request);
        String email = tokenService.getSubject(token);
        CreationToken creationToken = creationTokenService.generateToken(email);
        System.out.println(creationToken);
        return ResponseEntity.ok(creationToken);
    }


}
