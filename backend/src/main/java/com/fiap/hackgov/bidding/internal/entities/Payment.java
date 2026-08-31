package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@org.hibernate.annotations.Filter(name = "cityHallFilter", condition = BiddingScopeConditions.DECLARATION_CITY)
@org.hibernate.annotations.Filter(name = "sectorFilter", condition = BiddingScopeConditions.DECLARATION_SECTOR)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declaration_id", nullable = false)
    private PaymentDeclaration declaration;

    @Column(name = "payment_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal value;

    @Column(nullable = false)
    private Boolean treasuryApproved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treasury_responsible_id", nullable = false)
    private Employee treasuryResponsible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treasury_sector_id", nullable = false)
    private Sector treasurySector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id", nullable = false)
    private Employee approvedBy;

    @Column(nullable = false)
    private LocalDate paidAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
