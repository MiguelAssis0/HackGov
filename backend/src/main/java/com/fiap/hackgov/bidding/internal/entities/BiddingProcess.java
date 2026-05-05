package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.BiddingStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessType;
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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "bidding_processes")
public class BiddingProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "requisition_id")
    private Requisition requisition;

    @Enumerated(EnumType.STRING)
    private ProcessType type;

    private Date legalDeadline;
    private Date openingDate;

    @OneToOne
    @JoinColumn(name = "edital_id")
    private Edital edital;
    @Enumerated(EnumType.STRING)
    private BiddingStatus status;
    private UUID responsibleId;

    @ManyToOne
    @JoinColumn(name = "winning_supplier_id")
    private Supplier winningSupplier;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
