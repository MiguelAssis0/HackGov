package com.fiap.hackgov.tasks.internal.DTOs;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class TaskDetailDTOs {
    private TaskDetailDTOs() {}
    public record TextRequest(@NotBlank @Size(max = 2000) String text) {}
    public record ChecklistRequest(@NotBlank @Size(max = 180) String title) {}
    public record ReorderRequest(@NotNull List<UUID> itemIds) {}
    public record ManualTimeRequest(@NotNull @PastOrPresent LocalDate referenceDate,
                                    @NotNull @DecimalMin("0.01") @DecimalMax("24.0") Double hours,
                                    @NotBlank @Size(max = 500) String observation) {}
    public record CommentResponse(UUID id, String text, UUID authorId, String authorName, LocalDateTime createdAt, LocalDateTime editedAt) {}
    public record ChecklistResponse(UUID id, String title, int orderIndex, boolean completed, UUID completedById, String completedByName, LocalDateTime completedAt) {}
    public record TimeResponse(UUID id, UUID employeeId, String employeeName, LocalDateTime startedAt, LocalDateTime finishedAt,
                               long durationSeconds, boolean active, boolean manual, LocalDate referenceDate, String observation) {}
    public record AttachmentResponse(UUID id, String originalName, String contentType, long size, UUID uploadedById, String uploadedByName, LocalDateTime createdAt) {}
    public record DetailResponse(List<CommentResponse> comments, List<ChecklistResponse> checklist,
                                 List<TimeResponse> timeEntries, List<AttachmentResponse> attachments) {}
}
