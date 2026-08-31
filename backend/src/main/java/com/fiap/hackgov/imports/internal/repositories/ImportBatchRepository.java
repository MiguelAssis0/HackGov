package com.fiap.hackgov.imports.internal.repositories;

import com.fiap.hackgov.imports.internal.entities.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, UUID> {
    Optional<ImportBatch> findByIdAndCityHall_Id(UUID id, UUID cityId);

    List<ImportBatch> findTop100ByCityHall_IdOrderByCreatedAtDesc(UUID cityId);
}
