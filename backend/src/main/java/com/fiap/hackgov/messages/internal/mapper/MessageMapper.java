package com.fiap.hackgov.messages.internal.mapper;

import com.fiap.hackgov.messages.internal.DTOs.MessageResponseDTO;
import com.fiap.hackgov.messages.internal.entities.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.fullName", target = "senderName")
    @Mapping(source = "conversation.id", target = "conversationId")
    MessageResponseDTO toDTO(Message message);
}