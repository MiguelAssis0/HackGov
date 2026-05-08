package com.fiap.hackgov.messages.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.services.EmployeeService;
import com.fiap.hackgov.messages.internal.DTOs.MessageResponseDTO;
import com.fiap.hackgov.messages.internal.DTOs.SendMessageRequestDTO;
import com.fiap.hackgov.messages.internal.entities.Conversation;
import com.fiap.hackgov.messages.internal.entities.Message;
import com.fiap.hackgov.messages.internal.mapper.MessageMapper;
import com.fiap.hackgov.messages.internal.repositories.ConversationParticipantRepository;
import com.fiap.hackgov.messages.internal.repositories.ConversationRepository;
import com.fiap.hackgov.messages.internal.repositories.MessageRepository;
import com.fiap.hackgov.shared.infra.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    private final ConversationRepository conversationRepository;

    private final ConversationParticipantRepository participantRepository;

    private final EmployeeService employeeService;

    private final TokenService tokenService;

    private final MessageMapper mapper;

    public Message save(SendMessageRequestDTO dto, HttpServletRequest request) {

        String token = tokenService.extractToken(request);

        String email = tokenService.getSubject(token);

        Employee sender = employeeService.findByEmail(email);

        return createMessage(sender, dto.conversationId(), dto.content());
    }

    public List<MessageResponseDTO> getLastMessages(UUID conversationId, int page, int size, HttpServletRequest request) {
        String token = tokenService.extractToken(request);
        String email = tokenService.getSubject(token);
        Employee employee = employeeService.findByEmail(email);

        Conversation conversation = conversationRepository.findByIdAndCityHallId(conversationId, employee.getCityHallId().getId()).orElseThrow(() -> new RuntimeException("Conversation not found"));

        boolean participant = participantRepository.existsByConversationAndEmployee(conversation, employee);

        if (!participant) {
            throw new RuntimeException("Access denied");
        }

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId).stream().map(mapper::toDTO).toList();
    }

    public Message createMessage(Employee sender, UUID conversationId, String content) {

        Conversation conversation = conversationRepository.findByIdAndCityHallId(conversationId, sender.getCityHallId().getId()).orElseThrow(() -> new RuntimeException("Conversation not found"));

        boolean participant = participantRepository.existsByConversationAndEmployee(conversation, sender);

        if (!participant) {

            throw new RuntimeException("Access denied");
        }

        Message message = new Message();

        message.setSender(sender);

        message.setConversation(conversation);

        message.setContent(content);

        message.setSentAt(LocalDateTime.now());

        message.setReadMessage(false);

        return messageRepository.save(message);
    }
}