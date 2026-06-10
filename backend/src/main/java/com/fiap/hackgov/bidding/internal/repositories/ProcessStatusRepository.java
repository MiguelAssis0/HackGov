package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessStatusRepository extends JpaRepository<ProcessStatus, UUID> {
}