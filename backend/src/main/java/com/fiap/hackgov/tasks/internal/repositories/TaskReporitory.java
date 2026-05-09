package com.fiap.hackgov.tasks.internal.repositories;

import com.fiap.hackgov.tasks.internal.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskReporitory extends JpaRepository<Task, UUID> {
}
