package com.fiap.hackgov.messages.internal.controllers;

import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.messages.internal.DTOs.chat.ChatDTO;
import com.fiap.hackgov.messages.internal.DTOs.chat.CreatePrivateChatDTO;
import com.fiap.hackgov.messages.internal.DTOs.group.CreateGroupChatDTO;
import com.fiap.hackgov.messages.internal.DTOs.message.MessageDTO;
import com.fiap.hackgov.messages.internal.services.ChatService;
import com.fiap.hackgov.messages.internal.services.MessageService;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.pagination.PageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<ChatDTO>> getChats(@AuthenticationPrincipal User authenticatedUser) {

        if (!(authenticatedUser instanceof Employee employee)) {
            throw new BusinessException("Only employees can access chats");
        }

        List<ChatDTO> response = chatService.getEmployeeChats(employee);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<PageResponseDTO<MessageDTO>> getMessages(

            @AuthenticationPrincipal User authenticatedUser,

            @PathVariable UUID chatId,

            Pageable pageable) {

        if (!(authenticatedUser instanceof Employee employee)) {

            throw new BusinessException("Only employees can access messages");
        }

        return ResponseEntity.ok(messageService.getChatMessages(employee, chatId, pageable));
    }

    @PostMapping("/private")
    public ResponseEntity<ChatDTO> createPrivateChat(@AuthenticationPrincipal User authenticatedUser, @RequestBody @Valid CreatePrivateChatDTO dto) {

        if (!(authenticatedUser instanceof Employee employee)) {
            throw new BusinessException("Only employees can create chats");
        }

        ChatDTO response = chatService.createPrivateChat(employee, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/group")
    public ResponseEntity<ChatDTO> createGroupChat(@AuthenticationPrincipal User authenticatedUser, @RequestBody @Valid CreateGroupChatDTO dto) {

        if (!(authenticatedUser instanceof Employee employee)) {
            throw new BusinessException("Only employees can create chats");
        }

        ChatDTO response = chatService.createGroupChat(employee, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}