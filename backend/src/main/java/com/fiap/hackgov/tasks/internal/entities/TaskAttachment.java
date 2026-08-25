package com.fiap.hackgov.tasks.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "task_attachments")
public class TaskAttachment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private Task task;
    @Column(nullable = false, length = 255) private String originalName;
    @Column(nullable = false, length = 120) private String contentType;
    @Column(nullable = false) private long size;
    @Lob @Basic(fetch = FetchType.LAZY) @Column(nullable = false) private byte[] content;
    @ManyToOne(fetch = FetchType.LAZY) private Employee uploadedBy;
    @CreationTimestamp private LocalDateTime createdAt;
}
