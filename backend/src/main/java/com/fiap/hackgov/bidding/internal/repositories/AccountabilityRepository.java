package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Accountability;
import com.fiap.hackgov.bidding.internal.entities.enums.InstallmentStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountabilityRepository extends JpaRepository<Accountability, UUID> {
    
    List<Accountability> findByProcessStage(ProcessStage processStage);
    
    List<Accountability> findByInstallmentStatus(InstallmentStatus installmentStatus);
    
    List<Accountability> findByResponsibleId(UUID responsibleId);
    
    @Query("SELECT a FROM Accountability a WHERE a.processStage = :stage AND a.installmentStatus = :status")
    List<Accountability> findByProcessStageAndInstallmentStatus(
            @Param("stage") ProcessStage processStage,
            @Param("status") InstallmentStatus installmentStatus
    );
    
    @Query("SELECT a FROM Accountability a WHERE a.responsibleId = :responsibleId AND a.processStage = :stage")
    List<Accountability> findByResponsibleIdAndProcessStage(
            @Param("responsibleId") UUID responsibleId,
            @Param("stage") ProcessStage processStage
    );
}
