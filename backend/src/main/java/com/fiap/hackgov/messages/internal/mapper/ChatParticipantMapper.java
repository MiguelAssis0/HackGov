package com.fiap.hackgov.messages.internal.mapper;

import com.fiap.hackgov.messages.internal.DTOs.chat.ChatParticipantDTO;
import com.fiap.hackgov.messages.internal.entities.ChatParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatParticipantMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "fullName", source = "employee.fullName")
    @Mapping(target = "avatarPath", source = "employee.avatarPath")
    ChatParticipantDTO toDTO(ChatParticipant participant);
}
