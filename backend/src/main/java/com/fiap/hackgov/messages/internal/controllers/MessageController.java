package com.fiap.hackgov.messages.internal.controllers;

import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.messages.internal.DTOs.message.MessageDTO;
import com.fiap.hackgov.messages.internal.DTOs.message.SendMessageDTO;
import com.fiap.hackgov.messages.internal.services.MessageService;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(@AuthenticationPrincipal User authenticatedUser, @RequestBody @Valid SendMessageDTO dto) {

        if (!(authenticatedUser instanceof Employee employee)) {
            throw new BusinessException("Only employees can send messages");
        }

        MessageDTO response = messageService.sendMessage(employee, dto);
        messagingTemplate.convertAndSend("/topic/chat/" + dto.chatId(), response);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
