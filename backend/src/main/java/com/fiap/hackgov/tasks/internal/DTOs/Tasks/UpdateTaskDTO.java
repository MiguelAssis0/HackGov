package com.fiap.hackgov.tasks.internal.DTOs.Tasks;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateTok doaskDTO(
        @Size(min = 3, max = 120, message = "O titulo da tarefa deve ter entre 3 e 120 caracteres")
        String title,
        @Size(min = 3, max = 2000, message = "A descricao da tarefa deve ter entre 3 e 2000 caracteres")
        String description,
        UUID responsibleId,
        UUID boardId,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    @AssertTrue(message = "Informe ao menos um campo para atualizar a tarefa")
    public boolean hasAtLeastOneField() {
        return title != null ||
                description != null ||
                responsibleId != null ||
                boardId != null ||
                startDate != null ||
                endDate != null;
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
