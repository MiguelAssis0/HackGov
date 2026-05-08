package com.fiap.hackgov.messages.internal.repositories;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.messages.internal.entities.Chat;
import com.fiap.hackgov.messages.internal.entities.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {

    boolean existsByChatIdAndEmployeeId(UUID chatId, UUID employeeId);

    List<ChatParticipant> findByEmployee(Employee employee);

    List<ChatParticipant> findByChat(Chat chat);
}