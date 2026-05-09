package com.fiap.hackgov.tasks.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.tasks.internal.DTOs.Tasks.CreateTaskDTO;
import com.fiap.hackgov.tasks.internal.DTOs.Tasks.TaskResponseDTO;
import com.fiap.hackgov.tasks.internal.entities.Board;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.mapper.TaskMapper;
import com.fiap.hackgov.tasks.internal.repositories.BoardRepository;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskReporitory taskRepository;
    private final TaskMapper taskMapper;
    private final EmployeeRepository employeeRepository;
    private final BoardRepository boardRepository;

    public TaskResponseDTO create(CreateTaskDTO dto) {

        Employee responsible = employeeRepository.findById(dto.responsible().getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Board board = boardRepository.findById(dto.board().getId())
                .orElseThrow(() -> new RuntimeException("Board not found"));

        Task task = taskMapper.toEntity(dto);

        task.setResponsible(responsible);
        task.setBoard(board);

        return taskMapper.toDTO(taskRepository.save(task));
    }

    public Page<TaskResponseDTO> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(taskMapper::toDTO);
    }

    public TaskResponseDTO findById(UUID id) {
        return taskRepository.findById(id)
                .map(taskMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }
}