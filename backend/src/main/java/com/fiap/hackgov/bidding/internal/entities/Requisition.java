package com.fiap.hackgov.bidding.internal.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fiap.hackgov.bidding.internal.entities.enums.AcquisitionType;
import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private String registerNumber;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne
    @JoinColumn(name = "responsible_id")
    private Employee responsible;

    @ManyToOne
    @JoinColumn(name = "procurement_responsible_id")
    private Employee procurementResponsible;

    private String technicalDescription;

    private String justification;

    private String budgetAllocation;

    @Enumerated(EnumType.STRING)
    private AcquisitionType type;

    @Column(nullable = false)
    private boolean requiresEtp;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @OneToOne(
            mappedBy = "requisition",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private ETP etp;

    @OneToMany(mappedBy = "requisition")
    private List<Approval> approvals;

    @OneToMany(mappedBy = "requisition",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Analysis> analyses = new ArrayList<>();

    @OneToOne(mappedBy = "requisition",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private ProcessStatus processStatus;

    @OneToMany(mappedBy = "requisition",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ProcessHistory> histories = new ArrayList<>();

    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}
