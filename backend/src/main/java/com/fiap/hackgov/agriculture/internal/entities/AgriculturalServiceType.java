package com.fiap.hackgov.agriculture.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "agricultural_service_types", uniqueConstraints = @UniqueConstraint(name = "agri_type_city_name_uk", columnNames = {"city_hall_id", "name"}))
public class AgriculturalServiceType {
    public enum Area {URBAN, RURAL}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "city_hall_id")
    private CityHall cityHall;
    @Column(nullable = false, length = 140)
    private String name;
    @Enumerated(EnumType.STRING)
    private Area area;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyValue;
    private boolean active = true;
}
