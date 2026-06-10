package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Analysis;
import com.fiap.hackgov.bidding.internal.entities.enums.AnalysisResult;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    Page<Analysis> findByResult(AnalysisResult result, Pageable pageable);

    Optional<Analysis> findFirstByRequisitionIdAndResultOrderByCreatedAtDesc(UUID requisitionId, AnalysisResult result);

    Optional<Analysis> findFirstByRequisitionIdAndStageOrderByCreatedAtDesc(UUID requisitionId, ProcessStage stage);
}
