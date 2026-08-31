package com.fiap.hackgov.agriculture.internal.repositories;

import com.fiap.hackgov.agriculture.internal.entities.OperationalControl;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OperationalControlRepository extends JpaRepository<OperationalControl, UUID> {
    @EntityGraph(attributePaths = {"machinery", "tractorDriver"})
    Optional<OperationalControl> findByServiceRequest_Id(UUID serviceId);
}
