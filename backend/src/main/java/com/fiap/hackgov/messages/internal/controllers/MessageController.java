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
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ContentDisposition;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import com.fiap.hackgov.messages.internal.entities.MessageAttachment;

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

    @PostMapping(value="/attachment",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDTO> sendAttachment(@AuthenticationPrincipal Employee employee,@RequestParam UUID chatId,@RequestParam(defaultValue="")String content,@RequestPart("file")MultipartFile file){MessageDTO response=messageService.sendAttachment(employee,chatId,content,file);messagingTemplate.convertAndSend("/topic/chat/"+chatId,response);return ResponseEntity.status(HttpStatus.CREATED).body(response);}

    @GetMapping("/chats/{chatId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> download(@PathVariable UUID chatId,@PathVariable UUID attachmentId,@AuthenticationPrincipal Employee employee){MessageAttachment file=messageService.downloadAttachment(chatId,attachmentId,employee);return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).contentLength(file.getSizeBytes()).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(file.getOriginalName()).build().toString()).body(file.getContent());}
}
