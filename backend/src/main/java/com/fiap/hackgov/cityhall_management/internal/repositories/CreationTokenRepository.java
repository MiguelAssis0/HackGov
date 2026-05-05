package com.fiap.hackgov.cityhall_management.internal.repositories;

import com.fiap.hackgov.cityhall_management.internal.entities.CreationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreationTokenRepository extends JpaRepository<CreationToken, UUID> {
    Optional<CreationToken> findByIdAndUsedFalse(UUID id);
}
