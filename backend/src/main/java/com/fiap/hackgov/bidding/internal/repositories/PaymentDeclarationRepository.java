package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.PaymentDeclaration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentDeclarationRepository extends JpaRepository<PaymentDeclaration, UUID> {

    @Override
    @EntityGraph(attributePaths = {"commitment", "commitment.contract", "approvedBy"})
    Page<PaymentDeclaration> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"commitment", "commitment.contract", "approvedBy"})
    Optional<PaymentDeclaration> findById(UUID id);

    @EntityGraph(attributePaths = {"commitment", "commitment.contract", "approvedBy"})
    Page<PaymentDeclaration> findAllByCommitmentId(UUID commitmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"commitment", "commitment.contract", "approvedBy"})
    Optional<PaymentDeclaration> findFirstByCommitmentContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(UUID requisitionId);
}
