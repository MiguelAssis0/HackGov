package com.fiap.hackgov.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.UUID;
import org.hibernate.validator.constraints.br.CPF;

import com.fiap.hackgov.entities.enums.Roles;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@MappedSuperclass
public abstract class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;

    @Column(unique = true)
    private String email;

    private String password;
    private boolean status;

    @Enumerated(EnumType.STRING)
    private Roles role;

    private String avatarPath;
    private String phone;
    private boolean twoFactor = false;
    private LocalDateTime lastLogin = null;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
