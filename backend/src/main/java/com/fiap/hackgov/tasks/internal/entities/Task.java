package com.fiap.hackgov.tasks.internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tasks")
@Filter(name = "cityHallFilter", condition = "board_id in (select b.id from boards b where b.city_hall_id = :cityHallId)")
@Filter(name = "sectorFilter", condition = "board_id in (select b.id from boards b where b.sector_id = :sectorId)")
public class Task {
    public enum Status {TODO, IN_PROGRESS, IN_REVIEW, COMPLETED}

    public enum Priority {LOW, NORMAL, HIGH, URGENT}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String description;

    @ManyToOne
    private Employee responsible;

    @ManyToMany
    @JoinTable(
            name = "task_responsibles",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id"),
            uniqueConstraints = @UniqueConstraint(name = "task_responsible_uk", columnNames = {"task_id", "employee_id"})
    )
    private Set<Employee> responsibles = new LinkedHashSet<>();

    @ManyToOne
    private Employee createdBy;

    @ManyToOne
    @JsonIgnore
    private Board board;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.NORMAL;

    private int businessPoints;

    @Column(length = 60)
    private String protocol = "";

    @Column(columnDefinition = "TEXT")
    private String expectedResult = "";

    private LocalDateTime completedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
