package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.InstallmentStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "accountabilities")
public class Accountability {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ProcessStage processStage;
    @Enumerated(EnumType.STRING)
    private InstallmentStatus installmentStatus;
    private UUID responsibleId;
    private LocalDateTime analysisDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne
    private Effort effort;

    @OneToOne
    private PaymentStatement paymentStatement;
}
