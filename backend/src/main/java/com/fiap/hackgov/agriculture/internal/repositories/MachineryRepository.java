package com.fiap.hackgov.agriculture.internal.repositories;

import com.fiap.hackgov.agriculture.internal.entities.Machinery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MachineryRepository extends JpaRepository<Machinery, UUID> {
    List<Machinery> findByCityHall_IdOrderByNameAsc(UUID cityId);

    Optional<Machinery> findByIdAndCityHall_Id(UUID id, UUID cityId);
}
