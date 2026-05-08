package com.fiap.hackgov.messages.internal.controllers;

import com.fiap.hackgov.messages.internal.DTOs.MessageResponseDTO;
import com.fiap.hackgov.messages.internal.DTOs.SendMessageRequestDTO;
import com.fiap.hackgov.messages.internal.entities.Message;
import com.fiap.hackgov.messages.internal.services.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    @Operation(summary = "Send a message", security = @SecurityRequirement(name = "bearer-key"), description = "Send a message to a conversation")
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody SendMessageRequestDTO dto, HttpServletRequest request) {

        Message response = messageService.save(dto, request);

        URI location = URI.create("/api/chat/" + response.getId());

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get last messages from a conversation", security = @SecurityRequirement(name = "bearer-key"), description = "Get last messages from a conversation")
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable UUID conversationId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, HttpServletRequest request) {

        List<MessageResponseDTO> response = messageService.getLastMessages(conversationId, page, size, request);

        return ResponseEntity.ok(response);
    }
}