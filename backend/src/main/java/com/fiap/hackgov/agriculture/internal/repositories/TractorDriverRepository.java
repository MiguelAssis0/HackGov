package com.fiap.hackgov.agriculture.internal.repositories;

import com.fiap.hackgov.agriculture.internal.entities.TractorDriver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TractorDriverRepository extends JpaRepository<TractorDriver, UUID> {
    List<TractorDriver> findByCityHall_IdOrderByNameAsc(UUID cityId);

    Optional<TractorDriver> findByIdAndCityHall_Id(UUID id, UUID cityId);
}
