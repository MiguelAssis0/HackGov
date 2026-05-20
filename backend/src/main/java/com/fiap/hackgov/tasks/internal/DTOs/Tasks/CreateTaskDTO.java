package com.fiap.hackgov.tasks.internal.DTOs.Tasks;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.tasks.internal.entities.Board;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

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
        LocalDateTime endDate
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
