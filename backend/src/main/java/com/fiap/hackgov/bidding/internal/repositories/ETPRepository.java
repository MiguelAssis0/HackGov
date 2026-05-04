package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.ETP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ETPRepository extends JpaRepository<ETP, UUID> {
}
