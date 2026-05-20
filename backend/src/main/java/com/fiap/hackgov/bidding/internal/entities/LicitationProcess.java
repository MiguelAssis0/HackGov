package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.LicitationStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "licitation_processes")
public class LicitationProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String processNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id", nullable = false)
    private Requisition requisition;

    @Enumerated(EnumType.STRING)
    private LicitationType type;

    @Enumerated(EnumType.STRING)
    private LicitationStatus status;

    private BigDecimal estimatedValue;

    @Column(columnDefinition = "TEXT")
    private String objectDescription;

    private LocalDate openingDate;

    private LocalDate closingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_supplier_id")
    private Supplier winnerSupplier;

    @OneToOne(mappedBy = "licitationProcess", cascade = CascadeType.ALL, orphanRemoval = true)
    private Notice notice;

    @OneToOne(mappedBy = "licitationProcess", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contract contract;

    @OneToMany(mappedBy = "licitationProcess", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proposal> proposals = new ArrayList<>();

    @OneToMany(mappedBy = "licitationProcess", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LicitationHistory> histories = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
