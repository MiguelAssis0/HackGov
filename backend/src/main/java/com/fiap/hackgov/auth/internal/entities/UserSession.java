package com.fiap.hackgov.auth.internal.entities;

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
@Table(name = "user_sessions", indexes = @Index(name = "session_user_active_idx", columnList = "user_id,revoked_at"))
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, length = 64)
    private String refreshTokenHash = "";
    @Column(length = 80)
    private String ipAddress = "";
    @Column(length = 500)
    private String userAgent = "";
    @Column(length = 60)
    private String browser = "";
    @Column(length = 60)
    private String operatingSystem = "";
    @Column(length = 20)
    private String deviceType = "desktop";
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;

    public boolean active() {
        return revokedAt == null && expiresAt != null && expiresAt.isAfter(LocalDateTime.now());
    }
}
