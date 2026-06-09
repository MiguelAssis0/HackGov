package com.fiap.hackgov.messages.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.messages.internal.DTOs.message.MessageDTO;
import com.fiap.hackgov.messages.internal.DTOs.message.SendMessageDTO;
import com.fiap.hackgov.messages.internal.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageDTO dto, Principal principal) {

        Employee employee = (Employee) ((Authentication) principal).getPrincipal();

        MessageDTO response = messageService.sendMessage(employee, dto);

        messagingTemplate.convertAndSend("/topic/chat/" + dto.chatId(), response);
    }
}
