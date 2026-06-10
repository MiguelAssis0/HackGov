package com.fiap.hackgov.cityhall_management.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.enums.UF;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "states")
public class State implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;

    @Enumerated(EnumType.ORDINAL)
    private UF uf;

    @OneToMany(mappedBy = "state")
    private List<CityHall> cityHalls = new ArrayList<>();
}
