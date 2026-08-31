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

    @EntityGraph(attributePaths = {"destinationSector", "destinationEmployee", "assignedTo", "createdBy"})
    @Query("""
            select e from InboxEntry e
            where e.cityHall.id = :cityHallId
              and (e.destinationEmployee.id = :employeeId or e.assignedTo.id = :employeeId
                   or (:admin = true and (e.destinationEmployee is not null or e.assignedTo is not null)))
              and (:unreadOnly = false or e.readAt is null)
              and (:query = '' or lower(e.title) like lower(concat('%', :query, '%'))
                    or lower(e.description) like lower(concat('%', :query, '%')))
            order by e.createdAt desc
            """)
    Page<InboxEntry> findPessoal(
            @Param("cityHallId") UUID cityHallId,
            @Param("employeeId") UUID employeeId,
            @Param("admin") boolean admin,
            @Param("unreadOnly") boolean unreadOnly,
            @Param("query") String query,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"destinationSector", "destinationEmployee", "assignedTo", "createdBy"})
    @Query("""
            select e from InboxEntry e
            where e.cityHall.id = :cityHallId
              and e.destinationEmployee is null
              and (:sectorId is null or e.destinationSector.id = :sectorId)
              and (:admin = true or e.destinationSector.id = :employeeSectorId)
              and (:unreadOnly = false or e.readAt is null)
              and (:query = '' or lower(e.title) like lower(concat('%', :query, '%'))
                    or lower(e.description) like lower(concat('%', :query, '%')))
            order by e.createdAt desc
            """)
    Page<InboxEntry> findSetor(
            @Param("cityHallId") UUID cityHallId,
            @Param("sectorId") UUID sectorId,
            @Param("employeeSectorId") UUID employeeSectorId,
            @Param("admin") boolean admin,
            @Param("unreadOnly") boolean unreadOnly,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("select count(e) from InboxEntry e where e.cityHall.id = :cityHallId and e.readAt is null and (e.destinationEmployee.id = :employeeId or e.assignedTo.id = :employeeId or (:admin = true and (e.destinationEmployee is not null or e.assignedTo is not null)))")
    long countUnreadPessoal(@Param("cityHallId") UUID cityHallId, @Param("employeeId") UUID employeeId, @Param("admin") boolean admin);

    @Query("select count(e) from InboxEntry e where e.cityHall.id = :cityHallId and e.readAt is null and e.destinationEmployee is null and (:admin = true or e.destinationSector.id = :sectorId) and (:filterSectorId is null or e.destinationSector.id = :filterSectorId)")
    long countUnreadSetor(@Param("cityHallId") UUID cityHallId, @Param("sectorId") UUID sectorId, @Param("filterSectorId") UUID filterSectorId, @Param("admin") boolean admin);

    @EntityGraph(attributePaths = {"destinationSector", "destinationEmployee", "assignedTo", "createdBy", "cityHall"})
    Optional<InboxEntry> findByIdAndCityHall_Id(UUID id, UUID cityHallId);

    Optional<InboxEntry> findByCityHall_IdAndKey(UUID cityHallId, String key);
}
