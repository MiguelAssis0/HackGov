package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Edital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface EditalRepository extends JpaRepository<Edital, UUID> {
    
    List<Edital> findByNumber(String number);
    
    List<Edital> findByStatus(String status);
    
    List<Edital> findByResponsibleId(UUID responsibleId);
    
    @Query("SELECT e FROM Edital e WHERE e.publicationDate BETWEEN :startDate AND :endDate")
    List<Edital> findByPublicationDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    
    @Query("SELECT e FROM Edital e WHERE e.deadlineDate <= :deadlineDate AND e.status != 'CLOSED'")
    List<Edital> findActiveEditalsNearDeadline(@Param("deadlineDate") Date deadlineDate);
    
    @Query("SELECT e FROM Edital e WHERE e.openingDate BETWEEN :startDate AND :endDate")
    List<Edital> findByOpeningDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    
    @Query("SELECT e FROM Edital e WHERE e.description LIKE %:searchTerm% OR e.object LIKE %:searchTerm% OR e.number LIKE %:searchTerm%")
    List<Edital> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(e) FROM Edital e WHERE e.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT e FROM Edital e WHERE e.responsibleId = :responsibleId AND e.status = :status")
    List<Edital> findByResponsibleIdAndStatus(
            @Param("responsibleId") UUID responsibleId,
            @Param("status") String status
    );
    
    @Query("SELECT e FROM Edital e WHERE e.deadlineDate < CURRENT_DATE AND e.status != 'CLOSED'")
    List<Edital> findExpiredEditals();
}
