package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "workflow_status")
@Filter(name = "cityHallFilter", condition = BiddingScopeConditions.REQ_CHILD_CITY)
@Filter(name = "sectorFilter", condition = BiddingScopeConditions.REQ_CHILD_SECTOR)
public class ProcessStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ProcessStage stage;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    private UUID responsibleId;

    private String observation;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "requisition_id")
    private Requisition requisition;

}
