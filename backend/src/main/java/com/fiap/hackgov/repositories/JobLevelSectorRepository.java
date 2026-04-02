package com.fiap.hackgov.repositories;

import com.fiap.hackgov.entities.JobLevelSector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobLevelSectorRepository extends JpaRepository<JobLevelSector,  UUID > {
}
