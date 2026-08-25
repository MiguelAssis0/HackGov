package com.fiap.hackgov.clients.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.shared.infra.security.SensitiveStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "municipal_clients", uniqueConstraints = @UniqueConstraint(name = "client_city_cpf_uk", columnNames = {"city_hall_id", "cpf_lookup"}), indexes = @Index(name = "client_city_name_idx", columnList = "city_hall_id,full_name"))
public class Client {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "city_hall_id", nullable = false) private CityHall cityHall;
    @Column(name = "full_name", nullable = false, length = 180) private String fullName;
    @Column(length = 100) private String nickname = "";
    @Convert(converter = SensitiveStringConverter.class) @Column(nullable = false, length = 500) private String cpf;
    @Column(name = "cpf_lookup", nullable = false, length = 80) private String cpfLookup;
    @Convert(converter = SensitiveStringConverter.class) @Column(nullable = false, length = 500) private String phone;
    @Convert(converter = SensitiveStringConverter.class) @Column(length = 500) private String secondaryContact = "";
    @Convert(converter = SensitiveStringConverter.class) @Column(length = 1000) private String address = "";
    @Convert(converter = SensitiveStringConverter.class) @Column(length = 500) private String stateRegistration = "";
    @Convert(converter = SensitiveStringConverter.class) @Column(length = 500) private String caf = "";
    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp private LocalDateTime updatedAt;
}
