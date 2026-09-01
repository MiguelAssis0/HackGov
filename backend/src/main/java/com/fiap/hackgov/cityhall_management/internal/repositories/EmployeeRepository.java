package com.fiap.hackgov.cityhall_management.internal.repositories;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    long countByCityHallId_IdAndSectorId_Id(UUID cityHallId, UUID sectorId);

    long countByCityHallId_Id(UUID cityHallId);

    @EntityGraph(attributePaths = {
            "occupationId",
            "occupationId.permissions",
            "occupationId.permissions.pk",
            "occupationId.permissions.pk.permission"
    })
    @Query("SELECT e FROM Employee e WHERE e.email = :email")
    Optional<Employee> findByEmailWithPermissions(@Param("email") String email);

    @EntityGraph(attributePaths = {
            "cityHallId",
            "cityHallId.state",
            "sectorId",
            "occupationId"
    })
    @Query("SELECT e FROM Employee e WHERE e.id = :id")
    Optional<Employee> findByIdWithDetails(@Param("id") UUID id);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByCpf(String cpf);

    @EntityGraph(attributePaths = {"sectorId", "occupationId"})
    List<Employee> findAllByCityHallId_IdAndStatusTrueOrderByFirstNameAscLastNameAsc(UUID cityHallId);

    @EntityGraph(attributePaths = {"occupationId", "sectorId"})
    @Query("""
            SELECT e
            FROM Employee e
            WHERE e.cityHallId.id = :cityHallId
              AND e.id <> :employeeId
            ORDER BY e.firstName, e.lastName
            """)
    List<Employee> findChatContacts(
            @Param("cityHallId") UUID cityHallId,
            @Param("employeeId") UUID employeeId
    );

    @EntityGraph(attributePaths = {"sectorId"})
    @Query("""
            SELECT e
            FROM Employee e
            WHERE e.cityHallId.id = :cityHallId
              AND LOWER(e.sectorId.name) LIKE '%compras%'
              AND e.status = true
            ORDER BY e.firstName, e.lastName
            """)
    List<Employee> findActiveProcurementEmployees(@Param("cityHallId") UUID cityHallId);

    @EntityGraph(attributePaths = {"sectorId", "occupationId"})
    @Query("""
            SELECT DISTINCT e
            FROM Employee e
            JOIN e.occupationId.permissions relation
            WHERE e.cityHallId.id = :cityHallId
              AND relation.pk.permission.resource = 'approval.accountability'
              AND (
                    LOWER(e.sectorId.name) LIKE '%prest%'
                    OR LOWER(e.sectorId.name) LIKE '%conta%'
                    OR LOWER(e.sectorId.name) LIKE '%control%'
                    OR LOWER(e.sectorId.name) LIKE '%finance%'
                    OR LOWER(e.sectorId.name) LIKE '%fazenda%'
                    OR LOWER(e.sectorId.name) LIKE '%tesour%'
              )
              AND e.status = true
            ORDER BY e.firstName, e.lastName
            """)
    List<Employee> findActiveAccountabilityEmployees(@Param("cityHallId") UUID cityHallId);

}
