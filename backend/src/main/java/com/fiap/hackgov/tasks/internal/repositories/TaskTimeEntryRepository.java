package com.fiap.hackgov.tasks.internal.repositories;

import com.fiap.hackgov.tasks.internal.entities.TaskTimeEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskTimeEntryRepository extends JpaRepository<TaskTimeEntry, UUID> {
    @EntityGraph(attributePaths = "employee")
    List<TaskTimeEntry> findByTask_IdOrderByCreatedAtDesc(UUID taskId);

    Optional<TaskTimeEntry> findFirstByEmployee_IdAndManualFalseAndFinishedAtIsNull(UUID employeeId);

    Optional<TaskTimeEntry> findByIdAndTask_Id(UUID id, UUID taskId);

    List<TaskTimeEntry> findByTask_IdAndManualFalseAndFinishedAtIsNull(UUID taskId);
}
