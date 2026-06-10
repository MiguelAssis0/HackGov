package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicitationProcessRepository extends JpaRepository<LicitationProcess, UUID> {

    Optional<LicitationProcess> findByRequisitionId(UUID requisitionId);

    List<LicitationProcess> findByProcessNumberStartingWithOrderByProcessNumberDesc(String prefix, Pageable pageable);
}
