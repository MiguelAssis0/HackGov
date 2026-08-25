package com.fiap.hackgov.tasks.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "cross_sector_task_requests")
public class CrossSectorTaskRequest {
    public enum Status { PENDING, ACCEPTED, REJECTED, CANCELLED }
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(optional=false) private CityHall cityHall;
    @ManyToOne(optional=false) private Sector originSector;
    @ManyToOne(optional=false) private Sector destinationSector;
    @Column(nullable=false,length=160) private String title;
    @Column(columnDefinition="TEXT") private String description="";
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Task.Priority priority=Task.Priority.NORMAL;
    private LocalDate deadline;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status=Status.PENDING;
    @ManyToOne(optional=false) private Employee requestedBy;
    @ManyToOne private Employee answeredBy;
    @Column(columnDefinition="TEXT") private String feedback="";
    @OneToOne private Task generatedTask;
    @CreationTimestamp private LocalDateTime createdAt;
    private LocalDateTime answeredAt;
}
