package com.fiap.hackgov.tasks.internal.repositories;

import com.fiap.hackgov.tasks.internal.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskReporitory extends JpaRepository<Task, UUID> {
    @EntityGraph(attributePaths = {"board", "board.sector"})
    @Query("""
            select distinct task from Task task
            left join task.responsibles responsible
            where task.board.cityHall.id = :cityHallId
              and task.status <> :completed
              and (task.responsible.id = :employeeId or responsible.id = :employeeId)
            """)
    List<Task> findDashboardAssignedTasks(
            @Param("cityHallId") UUID cityHallId,
            @Param("employeeId") UUID employeeId,
            @Param("completed") Task.Status completed
    );

    @EntityGraph(attributePaths = {"board", "board.sector"})
    @Query("""
            select task from Task task
            where task.board.cityHall.id = :cityHallId
              and task.status <> :completed
              and task.endDate >= :monthStart
              and task.endDate < :monthEndExclusive
            """)
    List<Task> findDashboardCalendarTasks(
            @Param("cityHallId") UUID cityHallId,
            @Param("completed") Task.Status completed,
            @Param("monthStart") LocalDateTime monthStart,
            @Param("monthEndExclusive") LocalDateTime monthEndExclusive
    );

    @EntityGraph(attributePaths = {"board", "board.sector"})
    @Query("""
            select task from Task task
            where task.board.cityHall.id = :cityHallId
              and task.board.sector.id = :sectorId
              and task.status <> :completed
              and task.endDate >= :monthStart
              and task.endDate < :monthEndExclusive
            """)
    List<Task> findDashboardCalendarTasksForSector(
            @Param("cityHallId") UUID cityHallId,
            @Param("sectorId") UUID sectorId,
            @Param("completed") Task.Status completed,
            @Param("monthStart") LocalDateTime monthStart,
            @Param("monthEndExclusive") LocalDateTime monthEndExclusive
    );

    Page<Task> findAllByBoard_CityHall_Id(UUID cityHallId, Pageable pageable);

    Page<Task> findAllByBoard_CityHall_IdAndBoard_Sector_Id(UUID cityHallId, UUID sectorId, Pageable pageable);

    Optional<Task> findByIdAndBoard_CityHall_Id(UUID id, UUID cityHallId);

    Optional<Task> findByIdAndBoard_CityHall_IdAndBoard_Sector_Id(UUID id, UUID cityHallId, UUID sectorId);
}
