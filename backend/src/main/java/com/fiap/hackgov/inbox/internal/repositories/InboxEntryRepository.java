package com.fiap.hackgov.inbox.internal.repositories;

import com.fiap.hackgov.inbox.internal.entities.InboxEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InboxEntryRepository extends JpaRepository<InboxEntry, UUID> {
    @EntityGraph(attributePaths = {"destinationSector", "destinationEmployee", "assignedTo", "createdBy"})
    @Query("""
            select e from InboxEntry e
            where e.cityHall.id = :cityHallId
              and (:admin = true or e.destinationEmployee.id = :employeeId
                   or (e.destinationEmployee is null and e.destinationSector.id = :sectorId))
              and (:status is null or e.status = :status)
              and (:type is null or e.type = :type)
              and (:unreadOnly = false or e.readAt is null)
              and (:query = '' or lower(e.title) like lower(concat('%', :query, '%'))
                   or lower(e.description) like lower(concat('%', :query, '%')))
            order by e.createdAt desc
            """)
    Page<InboxEntry> findVisible(
            @Param("cityHallId") UUID cityHallId,
            @Param("employeeId") UUID employeeId,
            @Param("sectorId") UUID sectorId,
            @Param("admin") boolean admin,
            @Param("status") InboxEntry.Status status,
            @Param("type") InboxEntry.Type type,
            @Param("unreadOnly") boolean unreadOnly,
            @Param("query") String query,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"destinationSector", "destinationEmployee", "assignedTo", "createdBy", "cityHall"})
    Optional<InboxEntry> findByIdAndCityHall_Id(UUID id, UUID cityHallId);

    Optional<InboxEntry> findByCityHall_IdAndKey(UUID cityHallId, String key);
}
