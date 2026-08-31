package com.fiap.hackgov.inbox.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
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
@Table(name = "inbox_entries", uniqueConstraints = {
        @UniqueConstraint(name = "inbox_city_key_uk", columnNames = {"city_hall_id", "entry_key"})
}, indexes = {
        @Index(name = "inbox_city_created_idx", columnList = "city_hall_id,created_at"),
        @Index(name = "inbox_employee_status_idx", columnList = "destination_employee_id,status")
})
public class InboxEntry {
    public enum Type {DOCUMENT, ALERT, REQUEST, TASK}

    public enum Status {NEW, IN_PROGRESS, COMPLETED, ARCHIVED}

    public enum Priority {LOW, NORMAL, HIGH}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "city_hall_id", nullable = false)
    private CityHall cityHall;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type = Type.TASK;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_sector_id")
    private Sector destinationSector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_employee_id")
    private Employee destinationEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private Employee assignedTo;

    @Column(length = 80)
    private String toolSlug = "";

    @Column(length = 80)
    private String objectType = "";

    private UUID objectId;

    @Column(columnDefinition = "TEXT")
    private String metadata = "{}";

    @Column(length = 300)
    private String url = "";

    @Column(name = "entry_key", length = 240)
    private String key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Employee createdBy;

    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
