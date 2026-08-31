package com.fiap.hackgov.tasks.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "task_checklist_items", indexes = @Index(name = "checklist_task_order_idx", columnList = "task_id,item_order"))
public class TaskChecklistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(name = "item_order", nullable = false)
    private int orderIndex;
    private boolean completed;
    @ManyToOne(fetch = FetchType.LAZY)
    private Employee completedBy;
    private LocalDateTime completedAt;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
