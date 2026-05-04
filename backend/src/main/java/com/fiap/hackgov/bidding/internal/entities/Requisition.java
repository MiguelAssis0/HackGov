package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
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

    private UUID cityHallId;
    private UUID sectorId;
    private UUID responsibleId;
    private UUID approvedById;

    private String technicianDescription;

    private String justification;

    private String budgetAllocation;

    @Column(nullable = false)
    private boolean requiresEtp;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @OneToOne
    @JoinColumn(name = "etp_id")
    private ETP etp;

    @OneToMany(mappedBy = "requisition")
    private List<Approval> approvals;

    @OneToOne
    @JoinColumn(name = "process_state_id")
    private ProcessState processState;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
