package com.fiap.hackgov.clients.internal.repositories;

import com.fiap.hackgov.clients.internal.entities.ClientServiceRecord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientServiceRecordRepository extends JpaRepository<ClientServiceRecord, UUID> {
    @EntityGraph(attributePaths = "createdBy")
    List<ClientServiceRecord> findByClient_IdOrderByServiceDateDescCreatedAtDesc(UUID clientId);
}
