package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.LicitationEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationStatus;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "licitation_histories")
public class LicitationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitation_process_id", nullable = false)
    private LicitationProcess licitationProcess;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicitationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicitationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private Employee changedBy;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}