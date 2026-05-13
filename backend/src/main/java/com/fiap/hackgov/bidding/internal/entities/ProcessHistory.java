package com.fiap.hackgov.bidding.internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fiap.hackgov.bidding.internal.entities.enums.HistoryEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "process_histories")
public class ProcessHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessStage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HistoryEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id", nullable = false)
    @JsonIgnore
    private Requisition requisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private Employee changedBy;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
