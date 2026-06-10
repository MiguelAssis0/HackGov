package com.fiap.hackgov.messages.internal.mapper;

import com.fiap.hackgov.messages.internal.DTOs.message.MessageDTO;
import com.fiap.hackgov.messages.internal.entities.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "chat.id", target = "chatId")
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.fullName", target = "senderName")
    @Mapping(source = "sender.avatarPath", target = "senderAvatar")
    MessageDTO toDTO(Message message);
}