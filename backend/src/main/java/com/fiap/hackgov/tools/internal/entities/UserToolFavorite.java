package com.fiap.hackgov.tools.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_tool_favorites", uniqueConstraints = @UniqueConstraint(name = "favorite_employee_slug_uk", columnNames = {"employee_id", "tool_slug"}))
public class UserToolFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    @Column(name = "tool_slug", nullable = false, length = 80)
    private String toolSlug;
}
