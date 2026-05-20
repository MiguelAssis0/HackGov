package com.fiap.hackgov.tasks.internal.DTOs.Tasks;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.tasks.internal.entities.Board;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateTaskDTO(
        @NotBlank
        String title,
        @NotBlank
        String description,
        @NotBlank
        Employee responsible,
        @NotBlank
        Board board,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
