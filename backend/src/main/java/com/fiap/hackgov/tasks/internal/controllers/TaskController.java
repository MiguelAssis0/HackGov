package com.fiap.hackgov.tasks.internal.controllers;

import com.fiap.hackgov.tasks.internal.DTOs.Tasks.CreateTaskDTO;
import com.fiap.hackgov.tasks.internal.DTOs.Tasks.TaskResponseDTO;
import com.fiap.hackgov.tasks.internal.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@RequestBody @Valid CreateTaskDTO dto) {

        TaskResponseDTO response = taskService.create(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(taskService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.findById(id));
    }
}