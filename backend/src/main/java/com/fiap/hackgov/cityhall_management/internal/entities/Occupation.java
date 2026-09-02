package com.fiap.hackgov.cityhall_management.internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.LevelOccupation;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.TypeJobLevel;
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
public class Occupation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(length = 140)
    private String slug;

    private String description;

    @Enumerated(EnumType.STRING)
    private TypeJobLevel types;

    private LevelOccupation level;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "pk.occupation", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PermissionsOccupation> permissions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "sector_id_id")
    @JsonIgnore
    private Sector sectorId;

    @ManyToOne
    @JoinColumn(name = "city_hall_id")
    @JsonIgnore
    private CityHall cityHall;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
