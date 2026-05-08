package com.fiap.hackgov.messages.internal.repositories;

import com.fiap.hackgov.messages.internal.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByChatIdOrderBySentAtDesc(UUID chatId, Pageable pageable);

}