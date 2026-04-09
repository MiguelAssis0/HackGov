package com.fiap.hackgov.erp.internal.repositories;

import com.fiap.hackgov.erp.internal.entities.JobLevelSector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobLevelSectorRepository extends JpaRepository<JobLevelSector,  UUID > {
}
