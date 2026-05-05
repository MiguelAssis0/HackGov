package com.fiap.hackgov.auth.internal.entities;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String cpf;

    @Column(unique = true)
    private String email;

    @Column(nullable = false, length = 2000)
    private String password;

    private Boolean status;

    private Boolean accessibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles role;

    private String avatarPath;

    @Column(unique = true)
    private String phone;

    private Boolean twoFactor = false;

    private Boolean acceptTerms = true;

    private LocalDateTime lastLogin;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(status);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}