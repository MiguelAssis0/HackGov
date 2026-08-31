package com.fiap.hackgov.tools.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tool_configurations", uniqueConstraints = @UniqueConstraint(name = "tool_city_slug_uk", columnNames = {"city_hall_id", "slug"}))
public class ToolConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "city_hall_id", nullable = false)
    private CityHall cityHall;
    @Column(nullable = false, length = 80)
    private String slug;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 60)
    private String category;
    @ManyToOne
    @JoinColumn(name = "custom_category_id")
    private ToolCategory customCategory;
    @Column(nullable = false, length = 80)
    private String icon;
    @Column(nullable = false, length = 280)
    private String description;
    @Column(length = 120)
    private String route;
    @Column(nullable = false)
    private boolean mandatory;
    @Column(nullable = false)
    private boolean enabled;
    @Column(nullable = false)
    private boolean restricted;
}
