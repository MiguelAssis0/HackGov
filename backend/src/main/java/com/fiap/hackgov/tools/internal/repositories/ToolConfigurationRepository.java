package com.fiap.hackgov.tools.internal.repositories;

import com.fiap.hackgov.tools.internal.entities.ToolConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ToolConfigurationRepository extends JpaRepository<ToolConfiguration, UUID> {
    List<ToolConfiguration> findByCityHall_IdOrderByCategoryAscNameAsc(UUID cityId);

    Optional<ToolConfiguration> findByCityHall_IdAndSlug(UUID cityId, String slug);

    List<ToolConfiguration> findByCustomCategory_Id(UUID categoryId);

    long countByCustomCategory_Id(UUID categoryId);
}
