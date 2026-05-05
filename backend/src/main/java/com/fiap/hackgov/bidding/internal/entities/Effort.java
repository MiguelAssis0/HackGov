package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.KindCommitment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "efforts")
public class Effort {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private KindCommitment kindCommitment;

    @Column(precision = 15, scale = 2)
    private BigDecimal reservedValue;

    private UUID emitterId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @OneToOne
    private ExecutionOrder executionOrder;

    @OneToOne
    private PaymentStatement paymentStatement;
}
