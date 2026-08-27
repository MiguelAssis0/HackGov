package com.fiap.hackgov.cityhall_management.internal.repositories;

import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectorRepository extends JpaRepository<Sector, UUID> {

    long countByCityHall_Id(UUID cityHallId);

    Page<Sector> findAllByCityHall_Id(UUID cityHallId, Pageable pageable);

    Optional<Sector> findByIdAndCityHall_Id(UUID id, UUID cityHallId);

    Optional<Sector> findByNameAndCityHall_Id(String name, UUID cityHallId);
}
