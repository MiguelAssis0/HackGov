package com.fiap.hackgov.tasks.internal.repositories;

import com.fiap.hackgov.tasks.internal.entities.TaskAttachment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, UUID> {
    @EntityGraph(attributePaths = "uploadedBy")
    List<TaskAttachment> findByTask_IdOrderByCreatedAtDesc(UUID taskId);

    Optional<TaskAttachment> findByIdAndTask_Id(UUID id, UUID taskId);
}
