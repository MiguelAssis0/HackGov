package com.fiap.hackgov.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blacklisted_tokens")
public class BlacklistedToken {

    @Id
    @Column(nullable = false, length = 2000)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}