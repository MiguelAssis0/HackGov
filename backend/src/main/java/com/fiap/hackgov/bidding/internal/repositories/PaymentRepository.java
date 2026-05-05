package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
