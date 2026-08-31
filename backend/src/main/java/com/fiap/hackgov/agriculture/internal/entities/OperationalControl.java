package com.fiap.hackgov.agriculture.internal.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "agricultural_operational_controls")
public class OperationalControl {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private AgriculturalServiceRequest serviceRequest;
    @ManyToOne(fetch = FetchType.LAZY)
    private Machinery machinery;
    @ManyToOne(fetch = FetchType.LAZY)
    private TractorDriver tractorDriver;
    @Column(precision = 12, scale = 2)
    private BigDecimal initialHourMeter;
    @Column(precision = 12, scale = 2)
    private BigDecimal finalHourMeter;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public BigDecimal performedHours() {
        return initialHourMeter == null || finalHourMeter == null ? null : finalHourMeter.subtract(initialHourMeter);
    }
}
