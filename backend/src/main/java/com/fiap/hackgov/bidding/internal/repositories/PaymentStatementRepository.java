package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.PaymentStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentStatementRepository extends JpaRepository<PaymentStatement, UUID> {
    
    List<PaymentStatement> findByStatus(String status);
    
    List<PaymentStatement> findByDueDate(Date dueDate);
    
    List<PaymentStatement> findByDueDateBetween(Date startDate, Date endDate);
    
    @Query("SELECT ps FROM PaymentStatement ps WHERE ps.paymentDate IS NULL AND ps.dueDate <= :dueDate")
    List<PaymentStatement> findOverduePayments(@Param("dueDate") Date dueDate);
    
    @Query("SELECT ps FROM PaymentStatement ps WHERE ps.paymentDate IS NOT NULL AND ps.paymentDate BETWEEN :startDate AND :endDate")
    List<PaymentStatement> findPaidPaymentsBetween(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    
    @Query("SELECT SUM(ps.amount) FROM PaymentStatement ps WHERE ps.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(ps) FROM PaymentStatement ps WHERE ps.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT ps FROM PaymentStatement ps WHERE ps.amount BETWEEN :minAmount AND :maxAmount")
    List<PaymentStatement> findByAmountBetween(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount
    );
}
