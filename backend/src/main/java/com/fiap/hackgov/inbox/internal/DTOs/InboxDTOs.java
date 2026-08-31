package com.fiap.hackgov.inbox.internal.DTOs;

import com.fiap.hackgov.inbox.internal.entities.InboxEntry;

import java.time.LocalDateTime;
import java.util.UUID;

public final class InboxDTOs {
    private InboxDTOs() {
    }

    public record Response(
            UUID id, String title, String description, InboxEntry.Type type,
            InboxEntry.Status status, InboxEntry.Priority priority,
            UUID destinationSectorId, String destinationSectorName,
            UUID destinationEmployeeId, String destinationEmployeeName,
            UUID assignedToId, String assignedToName,
            String toolSlug, String objectType, UUID objectId, String url,
            String senderName, String metadata, LocalDateTime readAt, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
    }

    public record Counts(long minhasNaoLidas, long setorNaoLidas) {}
}
