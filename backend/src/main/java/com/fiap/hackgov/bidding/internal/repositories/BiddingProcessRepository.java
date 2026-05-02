package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.BiddingProcess;
import com.fiap.hackgov.bidding.internal.entities.enums.BiddingStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface BiddingProcessRepository extends JpaRepository<BiddingProcess, UUID> {
    
    List<BiddingProcess> findByType(ProcessType type);
    
    List<BiddingProcess> findByStatus(BiddingStatus status);
    
    List<BiddingProcess> findByResponsibleId(UUID responsibleId);
    
    List<BiddingProcess> findByWinningSupplierId(UUID winningSupplierId);
    
    @Query("SELECT bp FROM BiddingProcess bp WHERE bp.openingDate BETWEEN :startDate AND :endDate")
    List<BiddingProcess> findByOpeningDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    
    @Query("SELECT bp FROM BiddingProcess bp WHERE bp.legalDeadline <= :deadline AND bp.status != :completedStatus")
    List<BiddingProcess> findPendingProcessesNearDeadline(
            @Param("deadline") Date deadline,
            @Param("completedStatus") BiddingStatus completedStatus
    );
    
    @Query("SELECT bp FROM BiddingProcess bp WHERE bp.requisition.id = :requisitionId")
    BiddingProcess findByRequisitionId(@Param("requisitionId") UUID requisitionId);
    
    @Query("SELECT bp FROM BiddingProcess bp WHERE bp.edital.id = :editalId")
    BiddingProcess findByEditalId(@Param("editalId") UUID editalId);
    
    @Query("SELECT COUNT(bp) FROM BiddingProcess bp WHERE bp.status = :status")
    Long countByStatus(@Param("status") BiddingStatus status);
    
    @Query("SELECT bp FROM BiddingProcess bp WHERE bp.responsibleId = :responsibleId AND bp.status = :status")
    List<BiddingProcess> findByResponsibleIdAndStatus(
            @Param("responsibleId") UUID responsibleId,
            @Param("status") BiddingStatus status
    );
}
