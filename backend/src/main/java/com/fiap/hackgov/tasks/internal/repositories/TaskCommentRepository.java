package com.fiap.hackgov.tasks.internal.repositories;

import com.fiap.hackgov.tasks.internal.entities.TaskComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {
    @EntityGraph(attributePaths = "author")
    List<TaskComment> findByTask_IdOrderByCreatedAtAsc(UUID taskId);

    Optional<TaskComment> findByIdAndTask_Id(UUID id, UUID taskId);
}
