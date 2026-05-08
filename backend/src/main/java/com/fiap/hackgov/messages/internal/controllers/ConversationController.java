package com.fiap.hackgov.messages.internal.controllers;

import com.fiap.hackgov.messages.internal.DTOs.ConversationResponseDTO;
import com.fiap.hackgov.messages.internal.DTOs.CreateConversationRequestDTO;
import com.fiap.hackgov.messages.internal.entities.Conversation;
import com.fiap.hackgov.messages.internal.services.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "Create a new conversation", security = @SecurityRequirement(name = "bearer-key"), description = "Create a new conversation")
    @PostMapping
    public ResponseEntity<?> createConversation(@RequestBody CreateConversationRequestDTO dto, HttpServletRequest request) {

        Conversation response = conversationService.createConversation(dto, request);

        URI location = URI.create("/api/conversations/" + response.getId());

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get my conversations", security = @SecurityRequirement(name = "bearer-key"), description = "Get my conversations")
    @GetMapping
    public ResponseEntity<List<ConversationResponseDTO>> getMyConversations(HttpServletRequest request) {

        List<ConversationResponseDTO> response = conversationService.getMyConversations(request);
        return ResponseEntity.ok(response);
    }
}