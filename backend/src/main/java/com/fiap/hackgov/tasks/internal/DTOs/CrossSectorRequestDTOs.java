package com.fiap.hackgov.tasks.internal.DTOs;

import com.fiap.hackgov.tasks.internal.entities.CrossSectorTaskRequest;
import com.fiap.hackgov.tasks.internal.entities.Task;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class CrossSectorRequestDTOs {
    private CrossSectorRequestDTOs() {
    }

    public record Create(@NotNull UUID destinationSectorId, @NotBlank @Size(max = 160) String title,
                         @Size(max = 5000) String description, Task.Priority priority,
                         @FutureOrPresent LocalDate deadline) {
    }

    public record Answer(@Size(max = 2000) String feedback) {
    }

    public record Response(UUID id, UUID originSectorId, String originSectorName, UUID destinationSectorId,
                           String destinationSectorName, String title, String description, Task.Priority priority,
                           LocalDate deadline, CrossSectorTaskRequest.Status status, String requestedByName,
                           String answeredByName, String feedback, UUID generatedTaskId, LocalDateTime createdAt,
                           LocalDateTime answeredAt) {
    }
}
