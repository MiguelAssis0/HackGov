package com.fiap.hackgov.tasks.internal.DTOs.Board;

import com.fiap.hackgov.tasks.internal.entities.Task;

import java.util.List;
import java.util.UUID;

public record BoardResponseDTO(
        UUID id,
        String name,
        UUID cityHallId,
        UUID sector,
        List<Task> tasks
) {
}
