package com.fiap.hackgov.messages.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.services.EmployeeService;
import com.fiap.hackgov.messages.internal.DTOs.chat.ChatContactDTO;
import com.fiap.hackgov.messages.internal.DTOs.chat.ChatDTO;
import com.fiap.hackgov.messages.internal.DTOs.chat.CreatePrivateChatDTO;
import com.fiap.hackgov.messages.internal.DTOs.group.CreateGroupChatDTO;
import com.fiap.hackgov.messages.internal.entities.Chat;
import com.fiap.hackgov.messages.internal.entities.ChatParticipant;
import com.fiap.hackgov.messages.internal.entities.enums.ChatRole;
import com.fiap.hackgov.messages.internal.entities.enums.ChatType;
import com.fiap.hackgov.messages.internal.mapper.ChatMapper;
import com.fiap.hackgov.messages.internal.repositories.ChatParticipantRepository;
import com.fiap.hackgov.messages.internal.repositories.ChatRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    private final ChatParticipantRepository chatParticipantRepository;

    private final EmployeeRepository employeeRepository;

    private final EmployeeService employeeService;

    private final ChatMapper chatMapper;

    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ChatContactDTO> getContacts(Employee authenticatedEmployee) {
        if (authenticatedEmployee.getCityHallId() == null) {
            throw new BusinessException("Authenticated employee must be linked to a city hall");
        }

        entityManager.unwrap(Session.class).disableFilter("sectorFilter");

        return employeeRepository.findChatContacts(
                        authenticatedEmployee.getCityHallId().getId(),
                        authenticatedEmployee.getId()
                ).stream()
                .map(employee -> new ChatContactDTO(
                        employee.getId(),
                        employee.getFullName(),
                        employee.getAvatarPath(),
                        employee.getOccupationId() != null ? employee.getOccupationId().getName() : null,
                        employee.getSectorId() != null ? employee.getSectorId().getName() : null
                ))
                .toList();
    }

    @Transactional
    public ChatDTO createPrivateChat(Employee authenticatedEmployee, CreatePrivateChatDTO dto) {


        Employee target = employeeService.findById(dto.employeeId());

        if (authenticatedEmployee.getId().equals(target.getId())) {
            throw new BusinessException("You cannot create a chat with yourself");
        }

        Optional<Chat> existingChat = chatRepository.
                findPrivateChatBetweenEmployees(authenticatedEmployee.getId(), target.getId());

        if (existingChat.isPresent()) {
            return chatMapper.toDTO(existingChat.get());
        }

        LocalDateTime now = LocalDateTime.now();

        Chat chat = new Chat();
        chat.setType(ChatType.PRIVATE);
        chat.setCityHall(authenticatedEmployee.getCityHallId());
        chat.setCreatedAt(now);

        Chat savedChat = chatRepository.save(chat);

        ChatParticipant creator = new ChatParticipant();
        creator.setChat(savedChat);
        creator.setEmployee(authenticatedEmployee);
        creator.setJoinedAt(now);
        creator.setRole(ChatRole.MEMBER);

        ChatParticipant targetParticipant = new ChatParticipant();
        targetParticipant.setChat(savedChat);
        targetParticipant.setEmployee(target);
        targetParticipant.setJoinedAt(now);
        targetParticipant.setRole(ChatRole.MEMBER);

        chatParticipantRepository.saveAll(List.of(creator, targetParticipant));

        savedChat.getParticipants().add(creator);
        savedChat.getParticipants().add(targetParticipant);

        return chatMapper.toDTO(savedChat);
    }

    @Transactional
    public ChatDTO createGroupChat(Employee authenticatedEmployee, CreateGroupChatDTO dto) {

        entityManager.unwrap(Session.class).disableFilter("sectorFilter");
        List<Employee> foundParticipants = employeeRepository.findAllById(dto.participantIds());

        if (foundParticipants.size() != new HashSet<>(dto.participantIds()).size()) {
            throw new BusinessException("One or more participants not found");
        }

        Set<Employee> participants = new HashSet<>(foundParticipants);

        participants.add(authenticatedEmployee);
        Set<Employee> uniqueParticipants = new HashSet<>(participants);

        boolean invalidCityHall = uniqueParticipants.stream().anyMatch(employee -> !employee.getCityHallId().getId().equals(authenticatedEmployee.getCityHallId().getId()));

        if (invalidCityHall) {
            throw new BusinessException("All employees must belong to the same city hall");
        }

        LocalDateTime now = LocalDateTime.now();

        Chat chat = new Chat();
        chat.setTitle(dto.title());
        chat.setType(ChatType.GROUP);
        chat.setCityHall(authenticatedEmployee.getCityHallId());
        chat.setCreatedAt(now);

        Chat savedChat = chatRepository.save(chat);
        List<ChatParticipant> chatParticipants = uniqueParticipants.stream().map(employee -> {

            ChatParticipant participant = new ChatParticipant();

            participant.setChat(savedChat);
            participant.setEmployee(employee);
            participant.setJoinedAt(now);

            if (employee.getId().equals(authenticatedEmployee.getId())) {

                participant.setRole(ChatRole.ADMIN);

            } else {
                participant.setRole(ChatRole.MEMBER);
            }

            return participant;
        }).toList();
        chatParticipantRepository.saveAll(chatParticipants);
        savedChat.getParticipants().addAll(chatParticipants);
        return chatMapper.toDTO(savedChat);
    }

    @Transactional(readOnly = true)
    public List<ChatDTO> getEmployeeChats(Employee employee) {
        List<Chat> chats = chatRepository.findAllByParticipant(employee.getId());

        return chats.stream().map(chatMapper::toDTO).toList();
    }


}
