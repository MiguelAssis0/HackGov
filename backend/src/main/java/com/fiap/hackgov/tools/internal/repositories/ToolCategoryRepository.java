package com.fiap.hackgov.tools.internal.repositories;

import com.fiap.hackgov.tools.internal.entities.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ToolCategoryRepository extends JpaRepository<ToolCategory, UUID> {
    List<ToolCategory> findByCityHall_IdOrderByOrderAscNameAsc(UUID cityHallId);
    Optional<ToolCategory> findByIdAndCityHall_Id(UUID id, UUID cityHallId);
    boolean existsByCityHall_IdAndSlug(UUID cityHallId, String slug);
    boolean existsByCityHall_IdAndSlugAndIdNot(UUID cityHallId, String slug, UUID id);
}
