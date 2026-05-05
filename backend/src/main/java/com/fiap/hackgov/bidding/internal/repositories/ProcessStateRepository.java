package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.ProcessState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessStateRepository extends JpaRepository<ProcessState, UUID> {
}