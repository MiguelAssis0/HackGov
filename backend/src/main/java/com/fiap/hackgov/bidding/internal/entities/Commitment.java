package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.CommitmentType;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commitments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Commitment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_order_id")
    private ExecutionOrder executionOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommitmentType type;

    @Column(nullable = false, unique = true)
    private String commitmentNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal reservedValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_id", nullable = false)
    private Employee issuedBy;

    @OneToMany(mappedBy = "commitment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentDeclaration> paymentDeclarations = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
