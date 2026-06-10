package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.AccountabilityReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountabilityReportRepository extends JpaRepository<AccountabilityReport, UUID> {

    @Override
    @EntityGraph(attributePaths = {"contract", "responsible"})
    Page<AccountabilityReport> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"contract", "responsible"})
    Optional<AccountabilityReport> findById(UUID id);

    @EntityGraph(attributePaths = {"contract", "responsible"})
    Page<AccountabilityReport> findAllByContractId(UUID contractId, Pageable pageable);

    @EntityGraph(attributePaths = {"contract", "responsible"})
    Optional<AccountabilityReport> findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(UUID requisitionId);
}
