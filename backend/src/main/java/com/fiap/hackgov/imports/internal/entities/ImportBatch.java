package com.fiap.hackgov.imports.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "import_batches")
public class ImportBatch {
    public enum Status {UPLOADED, VALIDATED, VALIDATION_FAILED, IMPORTED, IMPORT_FAILED}

    public enum Mode {CREATE, UPDATE, UPSERT}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false)
    private CityHall cityHall;
    @ManyToOne(optional = false)
    private Employee uploadedBy;
    @Column(nullable = false, length = 255)
    private String originalFileName;
    @Column(nullable = false, length = 30)
    private String targetModule;
    @Enumerated(EnumType.STRING)
    private Mode importMode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status = Status.UPLOADED;
    @Lob
    @Column(nullable = false)
    private byte[] originalFile;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String headersJson = "[]";
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rowsJson = "[]";
    @Lob
    @Column(columnDefinition = "TEXT")
    private String mappingJson = "{}";
    @Lob
    @Column(columnDefinition = "TEXT")
    private String reportJson = "{}";
    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private int createdRecords;
    private int updatedRecords;
    private int ignoredRows;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
