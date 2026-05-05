package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Accountability;
import com.fiap.hackgov.bidding.internal.entities.enums.InstallmentStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountabilityRepository extends JpaRepository<Accountability, UUID> {
    Page<Accountability> findByProcessStage(ProcessStage processStage, Pageable pageable);
    Page<Accountability> findByInstallmentStatus(InstallmentStatus installmentStatus, Pageable pageable);
    Page<Accountability> findByResponsibleId(UUID responsibleId, Pageable pageable);

    Page<Accountability> findByAnalysisDateBetween(Date start, Date end, Pageable pageable);
    Page<Accountability> findByAnalysisDateBefore(Date date, Pageable pageable);
    Page<Accountability> findByAnalysisDateAfter(Date date, Pageable pageable);

    Optional<Accountability> findByEffortId(UUID effortId);
    Optional<Accountability> findByPaymentStatementId(UUID paymentStatementId);

    boolean existsByEffortId(UUID effortId);
    boolean existsByPaymentStatementId(UUID paymentStatementId);
}
