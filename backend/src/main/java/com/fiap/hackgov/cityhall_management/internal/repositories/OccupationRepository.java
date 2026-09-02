package com.fiap.hackgov.cityhall_management.internal.repositories;

import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface OccupationRepository extends JpaRepository<Occupation, UUID> {
    long countBySectorId_CityHall_Id(UUID cityHallId);

    Page<Occupation> findAllBySectorId_CityHall_Id(UUID cityHallId, Pageable pageable);

    Page<Occupation> findAllByCityHall_Id(UUID cityHallId, Pageable pageable);

    List<Occupation> findAllBySectorId_CityHall_Id(UUID cityHallId);

    List<Occupation> findAllByCityHall_Id(UUID cityHallId);

    Optional<Occupation> findByIdAndCityHall_Id(UUID id, UUID cityHallId);

    boolean existsByCityHall_IdAndSectorIdIsNullAndSlug(UUID cityHallId, String slug);

    boolean existsByCityHall_IdAndSectorId_IdAndSlug(UUID cityHallId, UUID sectorId, String slug);

    boolean existsByCityHall_IdAndSectorIdIsNullAndSlugAndIdNot(UUID cityHallId, String slug, UUID id);

    boolean existsByCityHall_IdAndSectorId_IdAndSlugAndIdNot(UUID cityHallId, UUID sectorId, String slug, UUID id);

    Optional<Occupation> findFirstByNameIgnoreCaseAndSectorId_CityHall_Id(String name, UUID cityHallId);
}
