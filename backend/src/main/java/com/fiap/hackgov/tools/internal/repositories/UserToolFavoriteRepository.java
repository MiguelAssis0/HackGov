package com.fiap.hackgov.tools.internal.repositories;

import com.fiap.hackgov.tools.internal.entities.UserToolFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserToolFavoriteRepository extends JpaRepository<UserToolFavorite, UUID> {
    List<UserToolFavorite> findByEmployee_Id(UUID employeeId);

    Optional<UserToolFavorite> findByEmployee_IdAndToolSlug(UUID employeeId, String slug);
}
