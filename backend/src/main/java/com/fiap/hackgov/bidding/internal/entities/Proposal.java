package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.ProposalImpugnationReason;
import com.fiap.hackgov.bidding.internal.entities.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "proposals")
@Filter(name = "cityHallFilter", condition = BiddingScopeConditions.LIC_CITY)
@Filter(name = "sectorFilter", condition = BiddingScopeConditions.LIC_SECTOR)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitation_process_id", nullable = false)
    private LicitationProcess licitationProcess;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal proposedValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status;

    @Enumerated(EnumType.STRING)
    private ProposalImpugnationReason impugnationReason;

    @Column(columnDefinition = "TEXT")
    private String impugnationDetails;

    @Column(columnDefinition = "TEXT")
    private String observation;

    private LocalDateTime submittedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
