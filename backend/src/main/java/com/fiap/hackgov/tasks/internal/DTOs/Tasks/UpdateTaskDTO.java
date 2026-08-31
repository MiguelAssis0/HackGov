package com.fiap.hackgov.tasks.internal.DTOs.Tasks;

import com.fiap.hackgov.tasks.internal.entities.Task;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UpdateTaskDTO(
        @Size(min = 3, max = 120, message = "O titulo da tarefa deve ter entre 3 e 120 caracteres")
        String title,
        @Size(min = 3, max = 2000, message = "A descricao da tarefa deve ter entre 3 e 2000 caracteres")
        String description,
        UUID responsibleId,
        UUID boardId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Task.Status status,
        Task.Priority priority,
        @Min(0) @Max(100) Integer businessPoints,
        @Size(max = 60) String protocol,
        @Size(max = 5000) String expectedResult,
        Set<UUID> responsibleIds
) {
    @AssertTrue(message = "Informe ao menos um campo para atualizar a tarefa")
    public boolean hasAtLeastOneField() {
        return title != null ||
                description != null ||
                responsibleId != null ||
                boardId != null ||
                startDate != null ||
                endDate != null ||
                status != null ||
                priority != null ||
                businessPoints != null ||
                protocol != null ||
                expectedResult != null ||
                responsibleIds != null;
    }

    @AssertTrue(message = "O titulo da tarefa nao pode ser vazio")
    public boolean isTitleValid() {
        return title == null || !title.isBlank();
    }

    @AssertTrue(message = "A descricao da tarefa nao pode ser vazia")
    public boolean isDescriptionValid() {
        return description == null || !description.isBlank();
    }

    @AssertTrue(message = "A data final da tarefa deve ser posterior ou igual a data inicial")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
