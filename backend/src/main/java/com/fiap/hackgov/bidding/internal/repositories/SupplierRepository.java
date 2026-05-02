package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    
    List<Supplier> findBySocialReasonContainingIgnoreCase(String socialReason);
    
    List<Supplier> findByCnpj(String cnpj);
    
    List<Supplier> findByEmail(String email);
    
    List<Supplier> findByIsActive(Boolean isActive);
    
    @Query("SELECT s FROM Supplier s WHERE s.socialReason LIKE %:searchTerm% OR s.cnpj LIKE %:searchTerm% OR s.email LIKE %:searchTerm%")
    List<Supplier> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT s FROM Supplier s WHERE s.bank = :bank AND s.bankAgency = :agency")
    List<Supplier> findByBankAndAgency(@Param("bank") String bank, @Param("agency") String agency);
    
    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.isActive = true")
    Long countActiveSuppliers();
    
    @Query("SELECT s FROM Supplier s JOIN s.biddingProcesses bp WHERE bp.id = :biddingProcessId")
    List<Supplier> findByBiddingProcessId(@Param("biddingProcessId") UUID biddingProcessId);
    
    @Query("SELECT s FROM Supplier s JOIN s.contracts c WHERE c.id = :contractId")
    Supplier findByContractId(@Param("contractId") UUID contractId);
}
