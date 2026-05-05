package com.fiap.hackgov.bidding.internal.entities;

import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
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
@Table(name = "process_states")
public class ProcessState {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ProcessStage currentStage;
    private int numberStep;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    private UUID responsibleId;

    private String observation;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "bidding_process_id")
    private Requisition biddingProcess;

}
