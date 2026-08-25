package com.fiap.hackgov.tasks.internal.mapper;

import com.fiap.hackgov.tasks.internal.DTOs.Tasks.CreateTaskDTO;
import com.fiap.hackgov.tasks.internal.DTOs.Tasks.TaskResponseDTO;
import com.fiap.hackgov.tasks.internal.entities.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "responsibles", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    Task toEntity(CreateTaskDTO dto);

    default TaskResponseDTO toDTO(Task task) {
        var board = task.getBoard();
        var sector = board == null ? null : board.getSector();
        var responsibleIds = task.getResponsibles().stream().map(employee -> employee.getId())
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        if (task.getResponsible() != null) responsibleIds.add(task.getResponsible().getId());
        return new TaskResponseDTO(
                task.getId(), task.getTitle(), task.getDescription(),
                task.getResponsible() == null ? null : task.getResponsible().getId(), responsibleIds,
                board == null ? null : board.getId(), sector == null ? null : sector.getId(), sector == null ? null : sector.getName(),
                task.getStartDate(), task.getEndDate(), task.getStatus(), task.getPriority(), task.getBusinessPoints(),
                task.getProtocol(), task.getExpectedResult(), task.getCompletedAt(), task.getCreatedAt(), task.getUpdatedAt()
        );
    }
}
