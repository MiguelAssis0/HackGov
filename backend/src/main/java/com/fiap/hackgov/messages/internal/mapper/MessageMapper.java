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
    @Mapping(source = "attachment.id", target = "attachmentId")
    @Mapping(source = "attachment.originalName", target = "attachmentName")
    @Mapping(source = "attachment.contentType", target = "attachmentContentType")
    @Mapping(source = "attachment.sizeBytes", target = "attachmentSize")
    MessageDTO toDTO(Message message);
}
