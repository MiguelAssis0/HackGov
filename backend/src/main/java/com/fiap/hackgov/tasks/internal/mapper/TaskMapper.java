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
    Task toEntity(CreateTaskDTO dto);

    @Mapping(target = "responsibleId", source = "responsible.id")
    @Mapping(target = "boardId", source = "board.id")
    TaskResponseDTO toDTO(Task task);
}
