package com.fiap.hackgov.cityhall_management.internal.repositories;

import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface OccupationRepository extends JpaRepository<Occupation, UUID> {
    Page<Occupation> findAllBySectorId_CityHall_Id(UUID cityHallId, Pageable pageable);
    Optional<Occupation> findFirstByNameIgnoreCaseAndSectorId_CityHall_Id(String name, UUID cityHallId);
}
