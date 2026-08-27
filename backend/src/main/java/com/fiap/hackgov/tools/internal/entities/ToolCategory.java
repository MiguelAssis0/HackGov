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
@Table(name = "tool_categories", uniqueConstraints = @UniqueConstraint(
        name = "tool_category_city_slug_uk", columnNames = {"city_hall_id", "slug"}
))
public class ToolCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "city_hall_id", nullable = false)
    private CityHall cityHall;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 80)
    private String slug;
    @Column(length = 500)
    private String description = "";
    @Column(nullable = false, length = 80)
    private String icon = "bi-folder-fill";
    @Column(name = "display_order", nullable = false)
    private int order;
    @Column(nullable = false)
    private boolean active = true;
}
