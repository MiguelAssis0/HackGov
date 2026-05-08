package com.fiap.hackgov.messages.internal.repositories;

import com.fiap.hackgov.messages.internal.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository
        extends JpaRepository<Message, UUID> {

    List<Message> findByConversationIdOrderBySentAtAsc(UUID conversationId);

    Message findTopByConversationIdOrderBySentAtDesc(
            UUID conversationId
    );
}