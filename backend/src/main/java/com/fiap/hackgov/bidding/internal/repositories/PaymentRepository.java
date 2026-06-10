package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Override
    @EntityGraph(attributePaths = {"declaration", "declaration.commitment", "declaration.commitment.contract", "treasuryResponsible", "treasurySector", "approvedBy"})
    Page<Payment> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"declaration", "declaration.commitment", "declaration.commitment.contract", "treasuryResponsible", "treasurySector", "approvedBy"})
    Optional<Payment> findById(UUID id);

    @EntityGraph(attributePaths = {"declaration", "declaration.commitment", "declaration.commitment.contract", "treasuryResponsible", "treasurySector", "approvedBy"})
    Page<Payment> findAllByDeclarationId(UUID declarationId, Pageable pageable);

    @EntityGraph(attributePaths = {"declaration", "declaration.commitment", "declaration.commitment.contract", "treasuryResponsible", "treasurySector", "approvedBy"})
    Optional<Payment> findFirstByDeclarationCommitmentContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(UUID requisitionId);

    boolean existsByDeclarationCommitmentContractId(UUID contractId);
}
