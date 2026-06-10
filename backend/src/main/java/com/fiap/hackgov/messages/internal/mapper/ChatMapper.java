package com.fiap.hackgov.messages.internal.mapper;

import com.fiap.hackgov.messages.internal.DTOs.chat.ChatDTO;
import com.fiap.hackgov.messages.internal.entities.Chat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = ChatParticipantMapper.class
)
public interface ChatMapper {

    @Mapping(source = "cityHall.id", target = "cityHallId")
    ChatDTO toDTO(Chat chat);
}
