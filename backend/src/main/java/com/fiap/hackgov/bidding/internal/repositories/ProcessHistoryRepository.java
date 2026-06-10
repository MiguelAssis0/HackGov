package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.ProcessHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessHistoryRepository extends JpaRepository<ProcessHistory, UUID> {
    List<ProcessHistory> findByRequisitionIdOrderByChangedAtAsc(UUID requisitionId);
}