package com.fiap.hackgov.cityhall_management.internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cityhalls")
public class CityHall implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;

    @Column(unique = true)
    private String cnpj;

    @ManyToOne(optional = false)
    @JoinColumn(name = "state_id")
    @JsonIgnore
    private State state;

    @OneToMany(mappedBy = "cityHall", cascade = CascadeType.ALL)
    @JsonIgnore
    private final List<Sector> sectors = new ArrayList<>();

    private boolean isActive;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cityHallId", cascade = CascadeType.ALL)
    @JsonIgnore
    private final List<Employee> employees = new ArrayList<>();
}