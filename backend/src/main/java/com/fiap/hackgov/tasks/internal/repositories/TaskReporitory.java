package com.fiap.hackgov.tasks.internal.repositories;

import com.fiap.hackgov.tasks.internal.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskReporitory extends JpaRepository<Task, UUID> {
    Page<Task> findAllByBoard_CityHall_Id(UUID cityHallId, Pageable pageable);

    Page<Task> findAllByBoard_CityHall_IdAndBoard_Sector_Id(UUID cityHallId, UUID sectorId, Pageable pageable);

    Optional<Task> findByIdAndBoard_CityHall_Id(UUID id, UUID cityHallId);

    Optional<Task> findByIdAndBoard_CityHall_IdAndBoard_Sector_Id(UUID id, UUID cityHallId, UUID sectorId);
}
