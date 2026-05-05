package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.DeclarationType;
import com.fiap.hackgov.bidding.internal.entities.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "payment_statements")
public class PaymentStatement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private DeclarationType declarationType;
    private UUID responsibleId;
    private Boolean approved;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    private Date dueDate;
    private Date paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "effort_id")
    private Effort effort;

    @OneToOne
    private Payment payment;
    


}
