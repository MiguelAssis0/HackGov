package com.fiap.hackgov.documents.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "municipal_documents", indexes = {
        @Index(name = "document_city_created_idx", columnList = "city_hall_id,created_at")
})
public class MunicipalDocument {
    public enum Visibility {PERSONAL, SECTOR, CITY_HALL}

    public enum SignatureStatus {NONE, PENDING, HOMOLOGATION, SIGNED}

    public enum Kind {SEND, SECTOR_FILE}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "city_hall_id", nullable = false)
    private CityHall cityHall;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Employee owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 60)
    private String documentType = "OTHER";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Kind kind = Kind.SEND;

    @Column(length = 30)
    private String number;

    @Column(name = "document_year")
    private Integer year;

    private LocalDate documentDate;

    @Column(length = 30)
    private String purpose;

    @Column(length = 255)
    private String keywords = "";

    @Column(length = 255)
    private String tags = "";

    @Column(columnDefinition = "TEXT")
    private String structuredContent = "";

    @Column(columnDefinition = "TEXT")
    private String description = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility = Visibility.PERSONAL;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] content;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(length = 80)
    private String sourceType;

    private UUID sourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id")
    private MunicipalDocument sourceDocument;

    @Column(length = 300)
    private String sourceUrl;

    @ManyToMany
    @JoinTable(name = "municipal_document_destinations",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id"))
    private Set<Employee> destinations = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "municipal_document_related_sectors",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "sector_id"))
    private Set<Sector> relatedSectors = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "municipal_document_related_employees",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id"))
    private Set<Employee> relatedEmployees = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "municipal_document_related_occupations",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "occupation_id"))
    private Set<Occupation> relatedOccupations = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SignatureStatus signatureStatus = SignatureStatus.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signed_by_id")
    private Employee signedBy;

    @Column(length = 64)
    private String signatureCode = "";

    @Column(length = 64)
    private String signatureHash = "";

    @Column(length = 30)
    private String signatureStandard = "";

    @Column(length = 180)
    private String signatureHolder = "";

    @Column(length = 255)
    private String signatureIssuer = "";

    @Column(length = 80)
    private String signatureCertificateSerial = "";

    @Column(length = 64)
    private String signatureCertificateFingerprint = "";

    private LocalDateTime signatureCertificateValidFrom;
    private LocalDateTime signatureCertificateValidUntil;

    @Column(nullable = false)
    private boolean signatureTimestampIncluded;

    @Column(length = 80)
    private String signatureProvider = "";

    @Column(length = 20)
    private String signatureEnvironment = "";

    @Column(length = 180)
    private String signatureExternalReference = "";

    private LocalDateTime signedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
