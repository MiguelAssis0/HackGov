package com.fiap.hackgov.tasks.internal.DTOs.Tasks;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.tasks.internal.entities.Board;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponseDTO(

        UUID id,

        String title,

        String description,

        Employee responsible,

        Board board,

        LocalDateTime startDate, LocalDateTime endDate,

        LocalDateTime createdAt,

        LocalDateTime updatedAt) {
}
