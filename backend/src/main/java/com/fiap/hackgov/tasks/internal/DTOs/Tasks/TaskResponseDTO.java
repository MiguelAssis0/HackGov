package com.fiap.hackgov.tasks.internal.DTOs.Tasks;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;
import com.fiap.hackgov.tasks.internal.entities.Task;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        UUID responsibleId,
        Set<UUID> responsibleIds,
        UUID boardId,
        UUID sectorId,
        String sectorName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Task.Status status,
        Task.Priority priority,
        int businessPoints,
        String protocol,
        String expectedResult,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
