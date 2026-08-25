package com.fiap.hackgov.audit.internal.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "audit_city_created_idx", columnList = "city_hall_id,created_at")
})
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_hall_id", nullable = false)
    private UUID cityHallId;

    private UUID actorId;

    @Column(nullable = false, length = 180)
    private String actorEmail;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(nullable = false)
    private int responseStatus;

    @Column(length = 80)
    private String remoteAddress;

    @Column(length = 400)
    private String userAgent;

    @Column(nullable = false, length = 64)
    private String previousHash;

    @Column(nullable = false, unique = true, length = 64)
    private String eventHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PreUpdate
    @PreRemove
    private void immutable() {
        throw new IllegalStateException("Registros de auditoria sao imutaveis");
    }
}
