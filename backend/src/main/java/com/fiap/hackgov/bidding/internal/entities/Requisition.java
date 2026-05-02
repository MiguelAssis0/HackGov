package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.enums.RequisitionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "requisitions")
public class Requisition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String number;

    private UUID cityhallId;
    private UUID sectorId;

    private String technicianDescription;
    private String justification;
    private String budgetAllocation;


    private RequisitionStatus status;

    private UUID responsibleId;
    private UUID approvedById;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "etp_id")
    private ETP etp;

    @OneToOne
    @JoinColumn(name = "approval_id")
    private Approval approval;

    @OneToOne
    @JoinColumn(name = "process_state_id")
    private ProcessState processState;

}
