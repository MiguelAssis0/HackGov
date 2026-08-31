package com.fiap.hackgov.clients.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.security.SensitiveStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "client_service_records")
public class ClientServiceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;
    @Column(nullable = false, length = 140)
    private String area;
    @Column(nullable = false, length = 280)
    private String description;
    @Convert(converter = SensitiveStringConverter.class)
    @Column(length = 2000)
    private String observation = "";
    private LocalDate serviceDate;
    @ManyToOne(fetch = FetchType.LAZY)
    private Employee createdBy;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
