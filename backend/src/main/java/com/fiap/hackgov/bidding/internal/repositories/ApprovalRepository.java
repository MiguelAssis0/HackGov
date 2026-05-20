package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Approval;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalSector;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApprovalRepository extends JpaRepository<Approval, UUID> {
    Page<Approval> findByApprovalStatus(ApprovalStatus status, Pageable pageable);

    Optional<Approval> findFirstByRequisitionIdAndApprovalSectorOrderByCreatedAtDesc(UUID requisitionId, ApprovalSector approvalSector);
}
