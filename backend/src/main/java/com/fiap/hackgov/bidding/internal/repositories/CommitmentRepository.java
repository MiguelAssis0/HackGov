package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Commitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommitmentRepository extends JpaRepository<Commitment, UUID> {

    @Override
    @EntityGraph(attributePaths = {"contract", "executionOrder", "issuedBy"})
    Page<Commitment> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"contract", "executionOrder", "issuedBy"})
    Optional<Commitment> findById(UUID id);

    @EntityGraph(attributePaths = {"contract", "executionOrder", "issuedBy"})
    Page<Commitment> findAllByContractId(UUID contractId, Pageable pageable);

    @EntityGraph(attributePaths = {"contract", "executionOrder", "issuedBy"})
    Page<Commitment> findAllByExecutionOrderId(UUID executionOrderId, Pageable pageable);

    boolean existsByCommitmentNumber(String commitmentNumber);
}
