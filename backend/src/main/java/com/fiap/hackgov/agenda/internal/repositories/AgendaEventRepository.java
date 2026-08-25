package com.fiap.hackgov.agenda.internal.repositories;

import com.fiap.hackgov.agenda.internal.entities.AgendaEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgendaEventRepository extends JpaRepository<AgendaEvent, UUID> {
    @EntityGraph(attributePaths = {"task"})
    @Query("""
            select e from AgendaEvent e
            where e.cityHall.id = :cityHallId
              and e.startDate <= :monthEnd
              and coalesce(e.endDate, e.startDate) >= :monthStart
              and (:taskId is null or e.task.id = :taskId)
            order by e.startDate, e.startTime, e.title
            """)
    List<AgendaEvent> findMonth(
            @Param("cityHallId") UUID cityHallId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd,
            @Param("taskId") UUID taskId
    );

    @EntityGraph(attributePaths = {"task"})
    @Query("""
            select e from AgendaEvent e
            where e.cityHall.id = :cityHallId
              and coalesce(e.endDate, e.startDate) >= :today
            order by e.startDate, e.startTime, e.title
            """)
    List<AgendaEvent> findUpcoming(@Param("cityHallId") UUID cityHallId, @Param("today") LocalDate today);

    @EntityGraph(attributePaths = {"task", "cityHall", "createdBy"})
    Optional<AgendaEvent> findByIdAndCityHall_Id(UUID id, UUID cityHallId);
}
