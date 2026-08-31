package com.fiap.hackgov.tasks.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "task_time_entries", indexes = @Index(name = "time_employee_active_idx", columnList = "employee_id,manual,finished_at"))
public class TaskTimeEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    private LocalDateTime startedAt;
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    private long durationSeconds;
    private boolean manual;
    private LocalDate referenceDate;
    @Column(length = 500)
    private String observation = "";
    @CreationTimestamp
    private LocalDateTime createdAt;
}
