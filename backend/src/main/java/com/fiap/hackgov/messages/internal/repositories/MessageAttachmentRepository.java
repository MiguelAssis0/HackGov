package com.fiap.hackgov.messages.internal.repositories;

import com.fiap.hackgov.messages.internal.entities.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, UUID> {
    Optional<MessageAttachment> findByIdAndMessage_Chat_Id(UUID id, UUID chatId);
}
