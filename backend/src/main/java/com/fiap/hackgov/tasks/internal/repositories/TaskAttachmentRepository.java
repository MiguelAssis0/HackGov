package com.fiap.hackgov.tasks.internal.repositories;
import com.fiap.hackgov.tasks.internal.entities.TaskAttachment;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, UUID> {
    @EntityGraph(attributePaths = "uploadedBy") List<TaskAttachment> findByTask_IdOrderByCreatedAtDesc(UUID taskId);
    Optional<TaskAttachment> findByIdAndTask_Id(UUID id, UUID taskId);
}
