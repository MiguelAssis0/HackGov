package com.fiap.hackgov.entities;

import com.fiap.hackgov.entities.enums.LevelJobLevel;
import com.fiap.hackgov.entities.enums.TypeJobLevel;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "job_levels")
public class JobLevel {
    @Id
    private UUID id;

    private String name;

    private String description;


    private final List<TypeJobLevel> types = new ArrayList<>();

    private LevelJobLevel level;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
