package com.fiap.hackgov.messages.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.services.EmployeeService;
import com.fiap.hackgov.messages.internal.DTOs.SendMessageRequestDTO;
import com.fiap.hackgov.messages.internal.DTOs.WebSocketMessageDTO;
import com.fiap.hackgov.messages.internal.entities.Message;
import com.fiap.hackgov.messages.internal.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    private final MessageService messageService;

    private final EmployeeService employeeService;

    @MessageMapping("/chat.send")
    public void sendMessage(
            SendMessageRequestDTO dto,
            SimpMessageHeaderAccessor headerAccessor
    ) {


        Principal principal =
                headerAccessor.getUser();

        System.out.println("PRINCIPAL:");

        System.out.println(principal);

        if (principal == null) {

            throw new RuntimeException(
                    "Usuário não autenticado"
            );
        }

        String email =
                principal.getName();

        System.out.println(email);

        Employee sender =
                employeeService.findByEmail(email);

        Message saved =
                messageService.createMessage(
                        sender,
                        dto.conversationId(),
                        dto.content()
                );

        WebSocketMessageDTO response =
                new WebSocketMessageDTO(
                        saved.getConversation().getId(),
                        saved.getContent(),
                        saved.getSender().getId(),
                        saved.getSender().getFullName(),
                        saved.getSentAt()
                );

        messagingTemplate.convertAndSend(
                "/topic/conversation/" +
                        dto.conversationId(),
                response
        );
    }
}