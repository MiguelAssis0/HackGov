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
            "sectorId",
            "occupationId"
    })
    @Query("SELECT e FROM Employee e WHERE e.id = :id")
    Optional<Employee> findByIdWithDetails(@Param("id") UUID id);

    Optional<Employee> findByEmail(String email);

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

}
