package com.fiap.hackgov.messages.internal.repositories;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.messages.internal.entities.Conversation;
import com.fiap.hackgov.messages.internal.entities.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationParticipantRepository
        extends JpaRepository<ConversationParticipant, UUID> {

    boolean existsByConversationAndEmployee(
            Conversation conversation,
            Employee employee
    );

    List<ConversationParticipant>
    findByEmployee(Employee employee);

    List<ConversationParticipant>
    findByConversation(Conversation conversation);
}