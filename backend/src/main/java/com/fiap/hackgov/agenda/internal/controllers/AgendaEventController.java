package com.fiap.hackgov.agenda.internal.controllers;

import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.Response;
import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.SaveRequest;
import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.Access;
import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.TaskDeadline;
import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.TaskOption;
import com.fiap.hackgov.agenda.internal.services.AgendaEventService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agenda/events")
@RequiredArgsConstructor
public class AgendaEventController {
    private final AgendaEventService service;

    @GetMapping
    public List<Response> findMonth(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) UUID taskId,
            @AuthenticationPrincipal Employee employee
    ) {
        return service.findMonth(month, taskId, employee);
    }

    @GetMapping("/tasks")
    public List<TaskDeadline> findTasks(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) UUID taskId,
            @AuthenticationPrincipal Employee employee
    ) {
        return service.findTasks(month, taskId, employee);
    }

    @GetMapping("/task-options")
    public List<TaskOption> findTaskOptions(@AuthenticationPrincipal Employee employee) {
        return service.findTaskOptions(employee);
    }

    @GetMapping("/access")
    public Access access(@AuthenticationPrincipal Employee employee) {
        return service.access(employee);
    }

    @GetMapping("/upcoming")
    public List<Response> findUpcoming(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal Employee employee
    ) {
        return service.findUpcoming(limit, employee);
    }

    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody SaveRequest request, @AuthenticationPrincipal Employee employee) {
        Response response = service.create(request, employee);
        return ResponseEntity.created(URI.create("/api/agenda/events/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public Response update(@PathVariable UUID id, @Valid @RequestBody SaveRequest request, @AuthenticationPrincipal Employee employee) {
        return service.update(id, request, employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        service.delete(id, employee);
        return ResponseEntity.noContent().build();
    }
}
