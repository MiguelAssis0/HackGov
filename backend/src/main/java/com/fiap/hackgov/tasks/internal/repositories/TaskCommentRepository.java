package com.fiap.hackgov.tasks.internal.repositories;
import com.fiap.hackgov.tasks.internal.entities.TaskComment;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {
    @EntityGraph(attributePaths = "author") List<TaskComment> findByTask_IdOrderByCreatedAtAsc(UUID taskId);
    Optional<TaskComment> findByIdAndTask_Id(UUID id, UUID taskId);
}
