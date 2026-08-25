package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.PaymentDeclarationType;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payment_declarations")
@org.hibernate.annotations.Filter(name="cityHallFilter",condition=BiddingScopeConditions.COMMITMENT_CITY)
@org.hibernate.annotations.Filter(name="sectorFilter",condition=BiddingScopeConditions.COMMITMENT_SECTOR)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commitment_id", nullable = false)
    private Commitment commitment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentDeclarationType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id", nullable = false)
    private Employee approvedBy;

    @Column(nullable = false)
    private Boolean secretaryApproved;

    @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
