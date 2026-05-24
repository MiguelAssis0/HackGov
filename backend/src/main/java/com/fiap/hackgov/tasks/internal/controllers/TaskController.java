package com.fiap.hackgov.tasks.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.tasks.internal.DTOs.Tasks.CreateTaskDTO;
import com.fiap.hackgov.tasks.internal.DTOs.Tasks.TaskResponseDTO;
import com.fiap.hackgov.tasks.internal.DTOs.Tasks.UpdateTaskDTO;
import com.fiap.hackgov.tasks.internal.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@AuthenticationPrincipal Employee employee, @RequestBody @Valid CreateTaskDTO dto) {

        TaskResponseDTO response = taskService.create(dto, employee);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> findAll(Pageable pageable, @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(taskService.findAll(pageable, employee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> findById(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(taskService.findById(id, employee));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid UpdateTaskDTO dto, @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(taskService.update(id, dto, employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        taskService.delete(id, employee);
        return ResponseEntity.noContent().build();
    }
}
