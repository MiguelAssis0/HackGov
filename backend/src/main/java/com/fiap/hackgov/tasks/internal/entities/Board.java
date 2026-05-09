package com.fiap.hackgov.tasks.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "boards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @ManyToOne
    private CityHall cityHall;

    @OneToMany(mappedBy = "board")
    private List<Task> tasks;

    @ManyToOne
    private Sector sector;
}
