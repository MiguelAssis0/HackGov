package com.fiap.hackgov.messages.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.services.EmployeeService;
import com.fiap.hackgov.messages.internal.DTOs.ConversationResponseDTO;
import com.fiap.hackgov.messages.internal.DTOs.CreateConversationRequestDTO;
import com.fiap.hackgov.messages.internal.entities.Conversation;
import com.fiap.hackgov.messages.internal.entities.ConversationParticipant;
import com.fiap.hackgov.messages.internal.entities.Message;
import com.fiap.hackgov.messages.internal.repositories.ConversationParticipantRepository;
import com.fiap.hackgov.messages.internal.repositories.ConversationRepository;
import com.fiap.hackgov.messages.internal.repositories.MessageRepository;
import com.fiap.hackgov.shared.infra.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    private final ConversationParticipantRepository participantRepository;

    private final EmployeeRepository employeeRepository;

    private final EmployeeService employeeService;

    private final MessageRepository messageRepository;
    private final TokenService tokenService;

    public Conversation createConversation(CreateConversationRequestDTO dto, HttpServletRequest request) {

        String token = tokenService.extractToken(request);
        String email = tokenService.getSubject(token);
        Employee creator = employeeService.findByEmail(email);

        Conversation conversation = new Conversation();

        conversation.setCityHall(creator.getCityHallId());

        conversation = conversationRepository.save(conversation);

        ConversationParticipant creatorParticipant = new ConversationParticipant();

        creatorParticipant.setConversation(conversation);

        creatorParticipant.setEmployee(creator);

        participantRepository.save(creatorParticipant);
        List<Employee> employees = employeeRepository.findAllById(dto.participantIds());

        for (Employee employee : employees) {
            if (!employee.getCityHallId().getId().equals(creator.getCityHallId().getId())) {

                throw new RuntimeException("Employee from another city hall");
            }

            ConversationParticipant participant = new ConversationParticipant();

            participant.setConversation(conversation);

            participant.setEmployee(employee);

            participantRepository.save(participant);
        }

        return conversation;
    }

    public List<ConversationResponseDTO> getMyConversations(HttpServletRequest request) {
        String token = tokenService.extractToken(request);
        String email = tokenService.getSubject(token);
        Employee employee = employeeService.findByEmail(email);


        List<ConversationParticipant> participations = participantRepository.findByEmployee(employee);

        return participations.stream().map(participation -> {

            Conversation conversation = participation.getConversation();


            List<String> participants = participantRepository.findByConversation(conversation).stream().map(p -> p.getEmployee().getUsername()).toList();


            Message lastMessage = messageRepository.findTopByConversationIdOrderBySentAtDesc(conversation.getId());

            return new ConversationResponseDTO(conversation.getId(), participants, lastMessage != null ? lastMessage.getContent() : null, lastMessage != null ? lastMessage.getSentAt() : null);
        }).toList();
    }
}