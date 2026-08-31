package com.fiap.hackgov.bidding.internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fiap.hackgov.bidding.internal.entities.enums.AnalysisResult;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
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
@Table(name = "analyses")
@org.hibernate.annotations.Filter(name = "cityHallFilter", condition = BiddingScopeConditions.REQ_CHILD_CITY)
@org.hibernate.annotations.Filter(name = "sectorFilter", condition = BiddingScopeConditions.REQ_CHILD_SECTOR)
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "requisition_id")
    @JsonIgnore
    private Requisition requisition;

    @Enumerated(EnumType.STRING)
    private ProcessStage stage;

    @Enumerated(EnumType.STRING)
    private AnalysisResult result;

    @ManyToOne
    @JoinColumn(name = "analyzed_by_id")
    private Employee analyzedBy;

    private String observation;

    private LocalDateTime analyzedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
