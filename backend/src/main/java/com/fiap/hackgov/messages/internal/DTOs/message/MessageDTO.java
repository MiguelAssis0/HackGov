package com.fiap.hackgov.messages.internal.DTOs.message;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageDTO(

        UUID id,

        UUID chatId,

        UUID senderId,

        String senderName,

        String senderAvatar,

        String content,

        LocalDateTime sentAt,

        UUID attachmentId,
        String attachmentName,
        String attachmentContentType,
        Long attachmentSize

) {
}
