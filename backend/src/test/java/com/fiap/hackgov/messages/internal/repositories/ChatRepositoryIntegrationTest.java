package com.fiap.hackgov.messages.internal.repositories;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class ChatRepositoryIntegrationTest {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void shouldReturnChatsForParticipant() {
        Employee joao = employeeRepository.findByEmail("joao@sp.gov.br")
                .orElseThrow();

        List<?> chats = chatRepository.findAllByParticipant(joao.getId());

        assertThat(chats).isNotEmpty();
    }

    @Test
    void shouldReturnChatsForDefaultAdminUser() {
        Employee adminSistema = employeeRepository.findByEmail("admin@admin.com")
                .orElseThrow();

        List<?> chats = chatRepository.findAllByParticipant(adminSistema.getId());

        assertThat(chats).isNotEmpty();
    }
}
