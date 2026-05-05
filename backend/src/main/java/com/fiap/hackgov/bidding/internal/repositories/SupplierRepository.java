package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Optional<Supplier> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);
}
