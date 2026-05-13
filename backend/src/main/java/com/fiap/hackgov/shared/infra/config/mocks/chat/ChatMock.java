package com.fiap.hackgov.shared.infra.config.mocks.chat;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.messages.internal.entities.Chat;
import com.fiap.hackgov.messages.internal.entities.ChatParticipant;
import com.fiap.hackgov.messages.internal.entities.enums.ChatRole;
import com.fiap.hackgov.messages.internal.entities.enums.ChatType;
import com.fiap.hackgov.messages.internal.repositories.ChatParticipantRepository;
import com.fiap.hackgov.messages.internal.repositories.ChatRepository;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMock {
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;

    public void load(MockContext ctx) {
        LocalDateTime now = LocalDateTime.now();
        Chat privateChat = new Chat();
        privateChat.setType(ChatType.PRIVATE);
        privateChat.setCityHall(ctx.cityHallSP);
        privateChat.setCreatedAt(now);
        chatRepository.save(privateChat);
        ChatParticipant admin = createParticipant(privateChat, ctx.admin, ChatRole.MEMBER, now);
        ChatParticipant joao = createParticipant(privateChat, ctx.joao, ChatRole.MEMBER, now);
        participantRepository.saveAll(List.of(admin, joao));
    }

    private ChatParticipant createParticipant(Chat chat, Employee employee, ChatRole role, LocalDateTime now) {
        ChatParticipant participant = new ChatParticipant();
        participant.setChat(chat);
        participant.setEmployee(employee);
        participant.setRole(role);
        participant.setJoinedAt(now);
        return participant;
    }
}