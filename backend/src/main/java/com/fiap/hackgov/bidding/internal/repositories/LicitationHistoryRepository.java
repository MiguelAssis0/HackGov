package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.LicitationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LicitationHistoryRepository extends JpaRepository<LicitationHistory, UUID> {

    List<LicitationHistory> findByLicitationProcessIdOrderByChangedAtAsc(UUID processId);
}