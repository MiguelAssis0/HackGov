package com.fiap.hackgov.AI.controllers;

import com.fiap.hackgov.AI.dtos.RequestAI;
import com.fiap.hackgov.AI.dtos.ResponseAI;
import com.fiap.hackgov.AI.services.AiService;
import com.fiap.hackgov.auth.internal.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/ai")
@AllArgsConstructor
public class AIController {

    private final AiService aiService;

    @Operation(summary = "Generate a response from AI", security = @SecurityRequirement(name = "bearer-key"))
    @PostMapping
    public ResponseEntity<ResponseAI> generateResponse(@RequestBody RequestAI request, @AuthenticationPrincipal User user) {
        ResponseAI response = aiService.generateResponse(request);
        return ResponseEntity.ok(response);
    }
}
