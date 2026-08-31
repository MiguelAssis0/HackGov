package com.fiap.hackgov.documents.internal.repositories;

import com.fiap.hackgov.documents.internal.entities.MunicipalDocument;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MunicipalDocumentRepository extends JpaRepository<MunicipalDocument, UUID> {
    List<MunicipalDocument> findByCityHall_IdAndSourceTypeAndSourceIdOrderByCreatedAtDesc(UUID cityHallId, String sourceType, UUID sourceId);

    @EntityGraph(attributePaths = {"owner", "sector", "destinations", "signedBy"})
    List<MunicipalDocument> findDistinctByCityHall_IdOrderByCreatedAtDesc(UUID cityHallId);

    @EntityGraph(attributePaths = {"owner", "sector", "destinations", "signedBy"})
    Optional<MunicipalDocument> findByIdAndCityHall_Id(UUID id, UUID cityHallId);
}
