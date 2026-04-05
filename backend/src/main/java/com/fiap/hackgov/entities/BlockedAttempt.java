package com.fiap.hackgov.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "blocked_attempts")
public class BlockedAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "\"key\"")
    private String key;

    @Column(nullable = false)
    private int totalAttempts;

    private LocalDateTime blockedUntil;

    private boolean permanentlyBlocked = false;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public BlockedAttempt(String key) {
        this.key = key;
        this.totalAttempts = 0;
        this.permanentlyBlocked = false;
        this.updatedAt = LocalDateTime.now();
    }
}