package com.fiap.hackgov.bidding.internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "approvals")
public class Approval {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "requisition_id")
    @JsonIgnore
    private Requisition requisition;

    @Enumerated(EnumType.STRING)
    private ApprovalStage stage;

    private UUID approvedById;

    private String observation;

    private LocalDateTime approvedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
