package com.fiap.hackgov.tools.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tool_permission_rules")
public class ToolPermissionRule {
    public enum Level {VIEW, MANAGE, ADMIN}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false)
    private CityHall cityHall;
    @Column(nullable = false, length = 80)
    private String toolSlug;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Sector sector;
    @ManyToOne
    private Occupation occupation;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Level level = Level.VIEW;
    @Column(nullable = false)
    private boolean enabled = true;
}
