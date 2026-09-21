package com.fiap.hackgov.shared.infra.config.mocks.chat;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.messages.internal.entities.Chat;
import com.fiap.hackgov.messages.internal.entities.ChatParticipant;
import com.fiap.hackgov.messages.internal.entities.Message;
import com.fiap.hackgov.messages.internal.entities.enums.ChatRole;
import com.fiap.hackgov.messages.internal.entities.enums.ChatType;
import com.fiap.hackgov.messages.internal.repositories.ChatParticipantRepository;
import com.fiap.hackgov.messages.internal.repositories.ChatRepository;
import com.fiap.hackgov.messages.internal.repositories.MessageRepository;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMock {
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final EmployeeRepository employeeRepository;

    private static final String CULTURA_MESSAGE = "Oi, Ana! Tudo bem? Você poderia dar uma olhadinha na caixa de entrada do setor para verificar se recebeu a tarefa referente à criação de um memorando para o Setor de Cultura organizar, junto com o Setor de Educação, um evento em comemoração ao Dia dos Professores?";

    public void load(MockContext ctx) {
        LocalDateTime now = LocalDateTime.now();
        createPrivateChat(ctx.cityHallSP, now, ctx.admin, ctx.joao);
        createPrivateChat(ctx.cityHallSP, now, ctx.adminSistema, ctx.admin);
        loadCulturaMessage(ctx.cityHallSP);
    }

    // ponytail: roda também em banco já populado — idempotente por chat + conteúdo
    public void loadCulturaMessage(CityHall cityHall) {
        Employee camila = employeeRepository.findByEmail("camila.cultura@sp.gov.br").orElse(null);
        Employee ana = employeeRepository.findByEmail("ana.compras@sp.gov.br").orElse(null);
        if (camila == null || ana == null) return;
        Chat chat = chatRepository.findPrivateChatBetweenEmployees(camila.getId(), ana.getId())
                .orElseGet(() -> createPrivateChat(cityHall, LocalDateTime.now(), camila, ana));
        boolean exists = messageRepository.findByChatIdOrderBySentAtDesc(chat.getId(), PageRequest.of(0, 50))
                .stream().anyMatch(item -> CULTURA_MESSAGE.equals(item.getContent()));
        if (exists) return;
        Message message = new Message();
        message.setChat(chat);
        message.setSender(camila);
        message.setContent(CULTURA_MESSAGE);
        message.setSentAt(LocalDateTime.now());
        messageRepository.save(message);
    }

    private Chat createPrivateChat(CityHall cityHall,
                                   LocalDateTime now,
                                   Employee firstParticipant,
                                   Employee secondParticipant) {
        Chat privateChat = new Chat();
        privateChat.setType(ChatType.PRIVATE);
        privateChat.setCityHall(cityHall);
        privateChat.setCreatedAt(now);
        chatRepository.save(privateChat);

        ChatParticipant first = createParticipant(privateChat, firstParticipant, ChatRole.MEMBER, now);
        ChatParticipant second = createParticipant(privateChat, secondParticipant, ChatRole.MEMBER, now);

        participantRepository.saveAll(List.of(first, second));
        return privateChat;
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
