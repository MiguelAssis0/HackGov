package com.fiap.hackgov.messages.internal.repositories;

import com.fiap.hackgov.messages.internal.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository
        extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByIdAndCityHallId(
            UUID conversationId,
            UUID cityHallId
    );
}