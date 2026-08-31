package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.ExecutionOrderType;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "execution_orders")
@org.hibernate.annotations.Filter(name = "cityHallFilter", condition = BiddingScopeConditions.CONTRACT_CITY)
@org.hibernate.annotations.Filter(name = "sectorFilter", condition = BiddingScopeConditions.CONTRACT_SECTOR)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionOrderType type;

    @Column(nullable = false, unique = true)
    private String number;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate issuedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_id", nullable = false)
    private Employee issuedBy;

    @OneToMany(mappedBy = "executionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commitment> commitments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
}
