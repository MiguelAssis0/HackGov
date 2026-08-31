package com.fiap.hackgov.agriculture.internal.repositories;

import com.fiap.hackgov.agriculture.internal.entities.PaymentProofType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentProofTypeRepository extends JpaRepository<PaymentProofType, UUID> {
    List<PaymentProofType> findByCityHall_IdOrderByNameAsc(UUID cityId);

    Optional<PaymentProofType> findByIdAndCityHall_Id(UUID id, UUID cityId);
}
