package com.fiap.hackgov.tools.internal.repositories;

import com.fiap.hackgov.tools.internal.entities.ToolPermissionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ToolPermissionRuleRepository extends JpaRepository<ToolPermissionRule, UUID> {
    List<ToolPermissionRule> findByCityHall_IdOrderByToolSlugAsc(UUID cityId);

    List<ToolPermissionRule> findByCityHall_IdAndToolSlug(UUID cityId, String slug);

    Optional<ToolPermissionRule> findByIdAndCityHall_Id(UUID id, UUID cityId);
}
