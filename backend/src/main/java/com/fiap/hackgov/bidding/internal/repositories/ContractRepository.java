package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    
    List<Contract> findByNumber(String number);
    
    List<Contract> findByResponsibleId(UUID responsibleId);
    
    List<Contract> findBySupplierId(UUID supplierId);
    
    @Query("SELECT c FROM Contract c WHERE c.amount BETWEEN :minAmount AND :maxAmount")
    List<Contract> findByAmountBetween(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount
    );
    
    @Query("SELECT c FROM Contract c WHERE c.startDate >= :startDate AND c.endDate <= :endDate")
    List<Contract> findByDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    
    @Query("SELECT c FROM Contract c WHERE c.biddingProcess.id = :biddingProcessId")
    Contract findByBiddingProcessId(@Param("biddingProcessId") UUID biddingProcessId);
    
    @Query("SELECT c FROM Contract c WHERE c.executionOrder.id = :executionOrderId")
    Contract findByExecutionOrderId(@Param("executionOrderId") UUID executionOrderId);
    
    @Query("SELECT c FROM Contract c WHERE c.effort.id = :effortId")
    Contract findByEffortId(@Param("effortId") UUID effortId);
    
    @Query("SELECT SUM(c.amount) FROM Contract c WHERE c.supplier.id = :supplierId")
    BigDecimal sumContractsBySupplierId(@Param("supplierId") UUID supplierId);
}
