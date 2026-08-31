package com.fiap.hackgov.messages.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.messages.internal.DTOs.message.MessageDTO;
import com.fiap.hackgov.messages.internal.DTOs.message.SendMessageDTO;
import com.fiap.hackgov.messages.internal.entities.Chat;
import com.fiap.hackgov.messages.internal.entities.Message;
import com.fiap.hackgov.messages.internal.entities.MessageAttachment;
import com.fiap.hackgov.messages.internal.mapper.MessageMapper;
import com.fiap.hackgov.messages.internal.repositories.ChatParticipantRepository;
import com.fiap.hackgov.messages.internal.repositories.ChatRepository;
import com.fiap.hackgov.messages.internal.repositories.MessageAttachmentRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    private final ChatRepository chatRepository;

    private final ChatParticipantRepository chatParticipantRepository;

    private final MessageMapper messageMapper;

    private final PaginationMapper paginationMapper;
    private final MessageAttachmentRepository attachmentRepository;

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

    @Transactional
    public MessageDTO sendAttachment(Employee employee, UUID chatId, String text, MultipartFile file) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        if (!chatParticipantRepository.existsByChatIdAndEmployeeId(chatId, employee.getId()))
            throw new BusinessException("You are not a participant of this chat");
        validateFile(file);
        Message message = new Message();
        message.setChat(chat);
        message.setSender(employee);
        message.setContent(text == null ? "" : text.trim());
        message.setSentAt(LocalDateTime.now());
        message = messageRepository.save(message);
        MessageAttachment attachment = new MessageAttachment();
        attachment.setMessage(message);
        attachment.setOriginalName(safe(file.getOriginalFilename()));
        attachment.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        attachment.setSizeBytes(file.getSize());
        try {
            attachment.setContent(file.getBytes());
        } catch (IOException e) {
            throw new BusinessException("Nao foi possivel ler o anexo");
        }
        message.setAttachment(attachmentRepository.save(attachment));
        return messageMapper.toDTO(message);
    }

    @Transactional(readOnly = true)
    public MessageAttachment downloadAttachment(UUID chatId, UUID attachmentId, Employee employee) {
        if (!chatParticipantRepository.existsByChatIdAndEmployeeId(chatId, employee.getId()))
            throw new ResourceNotFoundException("Anexo nao encontrado");
        return attachmentRepository.findByIdAndMessage_Chat_Id(attachmentId, chatId).orElseThrow(() -> new ResourceNotFoundException("Anexo nao encontrado"));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("Selecione um arquivo");
        if (file.getSize() > 10L * 1024 * 1024) throw new BusinessException("O anexo deve ter no maximo 10 MB");
        String name = safe(file.getOriginalFilename()).toLowerCase();
        if (Set.of(".exe", ".bat", ".cmd", ".sh", ".js", ".jar").stream().anyMatch(name::endsWith))
            throw new BusinessException("Tipo de arquivo nao permitido");
    }

    private String safe(String name) {
        if (name == null || name.isBlank()) return "anexo";
        String value = name.replace('\\', '/');
        return value.substring(value.lastIndexOf('/') + 1).replaceAll("[^a-zA-Z0-9._ -]", "_");
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
