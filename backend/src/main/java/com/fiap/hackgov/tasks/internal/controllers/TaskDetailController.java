package com.fiap.hackgov.tasks.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.tasks.internal.DTOs.TaskDetailDTOs.*;
import com.fiap.hackgov.tasks.internal.entities.TaskAttachment;
import com.fiap.hackgov.tasks.internal.services.TaskDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}")
@RequiredArgsConstructor
public class TaskDetailController {
    private final TaskDetailService service;

    @GetMapping("/details")
    public DetailResponse detail(@PathVariable UUID taskId, @AuthenticationPrincipal Employee employee) {
        return service.detail(taskId, employee);
    }

    @PostMapping("/comments")
    public CommentResponse addComment(@PathVariable UUID taskId, @Valid @RequestBody TextRequest request, @AuthenticationPrincipal Employee employee) {
        return service.addComment(taskId, request.text(), employee);
    }

    @PutMapping("/comments/{id}")
    public CommentResponse updateComment(@PathVariable UUID taskId, @PathVariable UUID id, @Valid @RequestBody TextRequest request, @AuthenticationPrincipal Employee employee) {
        return service.updateComment(taskId, id, request.text(), employee);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID taskId, @PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        service.deleteComment(taskId, id, employee);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checklist")
    public ChecklistResponse addChecklist(@PathVariable UUID taskId, @Valid @RequestBody ChecklistRequest request, @AuthenticationPrincipal Employee employee) {
        return service.addChecklist(taskId, request.title(), employee);
    }

    @PutMapping("/checklist/{id}")
    public ChecklistResponse updateChecklist(@PathVariable UUID taskId, @PathVariable UUID id, @Valid @RequestBody ChecklistRequest request, @AuthenticationPrincipal Employee employee) {
        return service.updateChecklist(taskId, id, request.title(), employee);
    }

    @PatchMapping("/checklist/{id}/toggle")
    public ChecklistResponse toggleChecklist(@PathVariable UUID taskId, @PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        return service.toggleChecklist(taskId, id, employee);
    }

    @PutMapping("/checklist/order")
    public List<ChecklistResponse> reorderChecklist(@PathVariable UUID taskId, @Valid @RequestBody ReorderRequest request, @AuthenticationPrincipal Employee employee) {
        return service.reorderChecklist(taskId, request.itemIds(), employee);
    }

    @DeleteMapping("/checklist/{id}")
    public ResponseEntity<Void> deleteChecklist(@PathVariable UUID taskId, @PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        service.deleteChecklist(taskId, id, employee);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/timer/start")
    public TimeResponse startTimer(@PathVariable UUID taskId, @AuthenticationPrincipal Employee employee) {
        return service.startTimer(taskId, employee);
    }

    @PostMapping("/timer/pause")
    public TimeResponse pauseTimer(@PathVariable UUID taskId, @AuthenticationPrincipal Employee employee) {
        return service.pauseTimer(taskId, employee);
    }

    @PostMapping("/time-entries")
    public TimeResponse addManualTime(@PathVariable UUID taskId, @Valid @RequestBody ManualTimeRequest request, @AuthenticationPrincipal Employee employee) {
        return service.addManualTime(taskId, request, employee);
    }

    @DeleteMapping("/time-entries/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable UUID taskId, @PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        service.deleteTime(taskId, id, employee);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachmentResponse addAttachment(@PathVariable UUID taskId, @RequestPart("file") MultipartFile file, @AuthenticationPrincipal Employee employee) {
        return service.addAttachment(taskId, file, employee);
    }

    @GetMapping("/attachments/{id}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable UUID taskId, @PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        TaskAttachment attachment = service.getAttachment(taskId, id, employee);
        ContentDisposition disposition = ContentDisposition.attachment().filename(attachment.getOriginalName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(attachment.getContentType()))
                .contentLength(attachment.getSize()).header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString()).body(attachment.getContent());
    }

    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID taskId, @PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        service.deleteAttachment(taskId, id, employee);
        return ResponseEntity.noContent().build();
    }
}
