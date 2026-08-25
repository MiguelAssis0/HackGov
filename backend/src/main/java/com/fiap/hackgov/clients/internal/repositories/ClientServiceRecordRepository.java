package com.fiap.hackgov.clients.internal.repositories;
import com.fiap.hackgov.clients.internal.entities.ClientServiceRecord;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface ClientServiceRecordRepository extends JpaRepository<ClientServiceRecord,UUID> {
 @EntityGraph(attributePaths="createdBy") List<ClientServiceRecord> findByClient_IdOrderByServiceDateDescCreatedAtDesc(UUID clientId);
}
