package com.fiap.hackgov.cityhall_management.internal.repositories;

import com.fiap.hackgov.cityhall_management.internal.entities.JobLevelSector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobLevelSectorRepository extends JpaRepository<JobLevelSector,  UUID > {
}
