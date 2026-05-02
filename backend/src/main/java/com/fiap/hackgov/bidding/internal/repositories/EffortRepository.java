package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Effort;
import com.fiap.hackgov.bidding.internal.entities.enums.KindCommitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface EffortRepository extends JpaRepository<Effort, UUID> {
    
    List<Effort> findByKindCommitment(KindCommitment kindCommitment);
    
    List<Effort> findByEmitterId(UUID emitterId);
    
    @Query("SELECT e FROM Effort e WHERE e.reservedValue BETWEEN :minValue AND :maxValue")
    List<Effort> findByReservedValueBetween(
            @Param("minValue") BigDecimal minValue,
            @Param("maxValue") BigDecimal maxValue
    );
    
    @Query("SELECT SUM(e.reservedValue) FROM Effort e WHERE e.emitterId = :emitterId")
    BigDecimal sumReservedValueByEmitterId(@Param("emitterId") UUID emitterId);
    
    @Query("SELECT e FROM Effort e WHERE e.contract.id = :contractId")
    Effort findByContractId(@Param("contractId") UUID contractId);
    
    @Query("SELECT e FROM Effort e WHERE e.executionOrder.id = :executionOrderId")
    Effort findByExecutionOrderId(@Param("executionOrderId") UUID executionOrderId);
}
