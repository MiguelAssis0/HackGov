package com.fiap.hackgov.audit.internal.repositories;

import com.fiap.hackgov.audit.internal.entities.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    Optional<AuditEvent> findTopByCityHallIdOrderByIdDesc(UUID cityHallId);

    List<AuditEvent> findTop500ByCityHallIdOrderByIdDesc(UUID cityHallId);
}
