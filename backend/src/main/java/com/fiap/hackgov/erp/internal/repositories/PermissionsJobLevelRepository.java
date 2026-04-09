package com.fiap.hackgov.erp.internal.repositories;

import com.fiap.hackgov.erp.internal.entities.PermissionsJobLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PermissionsJobLevelRepository extends JpaRepository<PermissionsJobLevel, UUID> {
}
