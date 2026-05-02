package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.ExecutionOrder;
import com.fiap.hackgov.bidding.internal.entities.enums.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionOrderRepository extends JpaRepository<ExecutionOrder, UUID> {
    
    List<ExecutionOrder> findByNumber(String number);
    
    List<ExecutionOrder> findByType(OrderType type);
    
    List<ExecutionOrder> findByStatus(String status);
    
    List<ExecutionOrder> findByResponsibleId(UUID responsibleId);
    
    @Query("SELECT eo FROM ExecutionOrder eo WHERE eo.emissionDate BETWEEN :startDate AND :endDate")
    List<ExecutionOrder> findByEmissionDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    
    @Query("SELECT eo FROM ExecutionOrder eo WHERE eo.expectedDeliveryDate <= :deliveryDate AND eo.status != 'DELIVERED'")
    List<ExecutionOrder> findPendingOrdersNearDelivery(@Param("deliveryDate") Date deliveryDate);
    
    @Query("SELECT eo FROM ExecutionOrder eo WHERE eo.actualDeliveryDate > eo.expectedDeliveryDate")
    List<ExecutionOrder> findDelayedOrders();
    
    @Query("SELECT COUNT(eo) FROM ExecutionOrder eo WHERE eo.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT eo FROM ExecutionOrder eo WHERE eo.responsibleId = :responsibleId AND eo.status = :status")
    List<ExecutionOrder> findByResponsibleIdAndStatus(
            @Param("responsibleId") UUID responsibleId,
            @Param("status") String status
    );
    
    @Query("SELECT eo FROM ExecutionOrder eo WHERE eo.expectedDeliveryDate BETWEEN :startDate AND :endDate")
    List<ExecutionOrder> findByExpectedDeliveryDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}
