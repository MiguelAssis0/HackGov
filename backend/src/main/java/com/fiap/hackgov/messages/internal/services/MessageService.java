package com.fiap.hackgov.messages.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.messages.internal.DTOs.message.MessageDTO;
import com.fiap.hackgov.messages.internal.DTOs.message.SendMessageDTO;
import com.fiap.hackgov.messages.internal.entities.Chat;
import com.fiap.hackgov.messages.internal.entities.Message;
import com.fiap.hackgov.messages.internal.mapper.MessageMapper;
import com.fiap.hackgov.messages.internal.repositories.ChatParticipantRepository;
import com.fiap.hackgov.messages.internal.repositories.ChatRepository;
import com.fiap.hackgov.messages.internal.repositories.MessageRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.pagination.PageResponseDTO;
import com.fiap.hackgov.shared.infra.pagination.PaginationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    private final ChatRepository chatRepository;

    private final ChatParticipantRepository chatParticipantRepository;

    private final MessageMapper messageMapper;

    private final PaginationMapper paginationMapper;

    @Transactional
    public MessageDTO sendMessage(Employee authenticatedEmployee, SendMessageDTO dto) {

        Chat chat = chatRepository.findById(dto.chatId()).orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        boolean participant = chatParticipantRepository.existsByChatIdAndEmployeeId(chat.getId(), authenticatedEmployee.getId());

        if (!participant) {
            throw new BusinessException("You are not a participant of this chat");
        }

        Message message = new Message();

        message.setChat(chat);
        message.setSender(authenticatedEmployee);
        message.setContent(dto.content());
        message.setSentAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        return messageMapper.toDTO(savedMessage);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<MessageDTO> getChatMessages(

            Employee authenticatedEmployee,

            UUID chatId,

            Pageable pageable) {

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        boolean isParticipant = chatParticipantRepository.existsByChatIdAndEmployeeId(chat.getId(), authenticatedEmployee.getId());

        if (!isParticipant) {

            throw new BusinessException("You are not a participant of this chat");
        }

        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Message> messages = messageRepository.findByChatIdOrderBySentAtDesc(chatId, safePageable);

        Page<MessageDTO> dtoPage = messages.map(messageMapper::toDTO);

        return paginationMapper.toDTO(dtoPage);
    }
}
