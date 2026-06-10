package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.ExecutionOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExecutionOrderRepository extends JpaRepository<ExecutionOrder, UUID> {

    @Override
    @EntityGraph(attributePaths = {"contract", "issuedBy"})
    Page<ExecutionOrder> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"contract", "issuedBy"})
    Optional<ExecutionOrder> findById(UUID id);

    @EntityGraph(attributePaths = {"contract", "issuedBy"})
    Page<ExecutionOrder> findAllByContractId(UUID contractId, Pageable pageable);

    @EntityGraph(attributePaths = {"contract", "issuedBy"})
    Optional<ExecutionOrder> findFirstByContractLicitationProcessRequisitionIdOrderByCreatedAtDesc(UUID requisitionId);

    boolean existsByNumber(String number);
}
