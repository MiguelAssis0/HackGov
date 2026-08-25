package com.fiap.hackgov.tasks.internal.repositories;
import com.fiap.hackgov.tasks.internal.entities.TaskChecklistItem;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface TaskChecklistRepository extends JpaRepository<TaskChecklistItem, UUID> {
    @EntityGraph(attributePaths = "completedBy") List<TaskChecklistItem> findByTask_IdOrderByOrderIndexAscCreatedAtAsc(UUID taskId);
    Optional<TaskChecklistItem> findByIdAndTask_Id(UUID id, UUID taskId);
    long countByTask_Id(UUID taskId);
}
