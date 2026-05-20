package com.fiap.hackgov.tasks.internal.DTOs.Tasks;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateTaskDTO(
        @Size(min = 3, max = 120, message = "O titulo da tarefa deve ter entre 3 e 120 caracteres")
        @Pattern(regexp = ".*\\S.*", message = "O titulo da tarefa nao pode ser vazio")
        String title,
        @Size(min = 3, max = 2000, message = "A descricao da tarefa deve ter entre 3 e 2000 caracteres")
        @Pattern(regexp = ".*\\S.*", message = "A descricao da tarefa nao pode ser vazia")
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

    @AssertTrue(message = "A data final da tarefa deve ser posterior ou igual a data inicial")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
