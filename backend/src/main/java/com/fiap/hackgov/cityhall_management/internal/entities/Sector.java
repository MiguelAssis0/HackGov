package com.fiap.hackgov.cityhall_management.internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
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
@Table(name = "sectors")
@Filter(name = "cityHallFilter", condition = "city_hall_id = :cityHallId")
public class Sector implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 140)
    private String slug;

    @Column(length = 1000)
    private String description = "";

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "cityHall_id")
    private CityHall cityHall;

    @ManyToOne
    @JsonIgnore
    private Occupation occupationId;

    @OneToMany
    @JsonIgnore
    private List<SectorTools> sectorTools = new ArrayList<>();


    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
