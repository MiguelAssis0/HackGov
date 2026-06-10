package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Requisition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequisitionRepository extends JpaRepository<Requisition, UUID> {

    List<Requisition>
    findByRegisterNumberStartingWithOrderByRegisterNumberDesc(
            String prefix,
            Pageable pageable
    );

}
