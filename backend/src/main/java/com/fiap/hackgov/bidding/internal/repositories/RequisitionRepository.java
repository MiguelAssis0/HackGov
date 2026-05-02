package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequisitionRepository extends JpaRepository<Requisition, UUID> {
    
    List<Requisition> findByNumber(String number);
    
    List<Requisition> findByStatus(RequestStatus status);
    
    List<Requisition> findByRequesterId(UUID requesterId);
    
    List<Requisition> findByApproverId(UUID approverId);
    
    @Query("SELECT r FROM Requisition r WHERE r.amount BETWEEN :minAmount AND :maxAmount")
    List<Requisition> findByAmountBetween(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount
    );
    
    @Query("SELECT r FROM Requisition r WHERE r.requestDate BETWEEN :startDate AND :endDate")
    List<Requisition> findByRequestDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    
    @Query("SELECT r FROM Requisition r WHERE r.approvalDate IS NULL AND r.status != 'CANCELADA'")
    List<Requisition> findPendingApprovals();
    
    @Query("SELECT COUNT(r) FROM Requisition r WHERE r.status = :status")
    Long countByStatus(@Param("status") RequestStatus status);
    
    @Query("SELECT SUM(r.amount) FROM Requisition r WHERE r.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") RequestStatus status);
    
    @Query("SELECT r FROM Requisition r WHERE r.requesterId = :requesterId AND r.status = :status")
    List<Requisition> findByRequesterIdAndStatus(
            @Param("requesterId") UUID requesterId,
            @Param("status") RequestStatus status
    );
    
    @Query("SELECT r FROM Requisition r WHERE r.description LIKE %:searchTerm% OR r.number LIKE %:searchTerm%")
    List<Requisition> findBySearchTerm(@Param("searchTerm") String searchTerm);
}
