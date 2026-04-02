package com.fiap.hackgov.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "job_levels_in_sectors")
public class JobLevelInSector {

    @Id
    @GeneratedValue()
    private UUID id;

    @JoinColumn(name = "sector_id", nullable = false)
    @ManyToOne
    private final List<Sector> sector = new ArrayList<>();

    @JoinColumn(name = "job_level_id", nullable = false)
    @ManyToOne
    private final List<JobLevel> jobLevel = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
