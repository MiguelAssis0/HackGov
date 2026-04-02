package com.fiap.hackgov.repositories;

import com.fiap.hackgov.entities.JobLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobLevelRepository extends JpaRepository<JobLevel, UUID> {
}
