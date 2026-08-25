package com.fiap.hackgov.tasks.internal.DTOs.Tasks;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.tasks.internal.entities.Board;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import com.fiap.hackgov.tasks.internal.entities.Task;

public record CreateTaskDTO(
        @NotBlank(message = "O titulo da tarefa e obrigatorio")
        @Size(min = 3, max = 120, message = "O titulo da tarefa deve ter entre 3 e 120 caracteres")
        String title,
        @NotBlank(message = "A descricao da tarefa e obrigatoria")
        @Size(min = 3, max = 2000, message = "A descricao da tarefa deve ter entre 3 e 2000 caracteres")
        String description,
        @Valid
        @NotNull(message = "O responsavel pela tarefa e obrigatorio")
        Employee responsible,
        @Valid
        @NotNull(message = "O quadro/setor de destino da tarefa e obrigatorio")
        Board board,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Task.Status status,
        Task.Priority priority,
        @Min(value = 0, message = "Os pontos de valor publico nao podem ser negativos")
        @Max(value = 100, message = "Os pontos de valor publico nao podem exceder 100")
        Integer businessPoints,
        @Size(max = 60) String protocol,
        @Size(max = 5000) String expectedResult,
        Set<UUID> responsibleIds
) {
    @AssertTrue(message = "O responsavel informado deve conter um id valido")
    public boolean isResponsibleValid() {
        return responsible == null || responsible.getId() != null;
    }

    @AssertTrue(message = "O quadro/setor informado deve conter um id valido")
    public boolean isBoardValid() {
        return board == null || board.getId() != null;
    }

    @AssertTrue(message = "A data final da tarefa deve ser posterior ou igual a data inicial")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
