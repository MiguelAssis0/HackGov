package com.fiap.hackgov.tasks.internal.DTOs.Tasks;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateTaskDTO(
        String title,
        String description,
        UUID responsibleId,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
