package com.fiap.hackgov.tasks.internal.repositories;

import com.fiap.hackgov.tasks.internal.entities.CrossSectorTaskRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrossSectorTaskRequestRepository extends JpaRepository<CrossSectorTaskRequest, UUID> {
    Optional<CrossSectorTaskRequest> findByIdAndCityHall_Id(UUID id, UUID cityHallId);

    List<CrossSectorTaskRequest> findByCityHall_IdOrderByCreatedAtDesc(UUID cityHallId);
}
