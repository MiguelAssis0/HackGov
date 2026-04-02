package com.fiap.hackgov.entities;

import com.fiap.hackgov.entities.enums.LevelJobLevel;
import com.fiap.hackgov.entities.enums.TypeJobLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
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
public class JobLevel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    private String name;

    private String description;

    @ElementCollection
    @CollectionTable(name = "job_level_types", joinColumns = @JoinColumn(name = "job_level_id"))
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private final List<TypeJobLevel> types = new ArrayList<>();

    private LevelJobLevel level;

    @OneToMany(mappedBy = "pk.jobLevel", cascade = CascadeType.ALL)
    private List<PermissionsJobLevel> permissions = new ArrayList<>();

    @OneToMany(mappedBy = "pk.jobLevel", cascade = CascadeType.ALL)
    private List<JobLevelSector> sectors = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}