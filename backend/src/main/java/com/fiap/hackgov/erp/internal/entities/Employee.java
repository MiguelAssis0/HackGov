package com.fiap.hackgov.erp.internal.entities;

import com.fiap.hackgov.erp.internal.entities.enums.RequisitionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employees")
public class Employee implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "pk.employee", cascade = CascadeType.ALL)
    private final List<EmployeeJobLevel> employeeJobLevels = new ArrayList<>();

    private Double salary;

    private LocalDateTime admissionDate;

    private RequisitionStatus requisitionStatus;

    private LocalDateTime dismissalDate;

    private String registrationNumber;

    private Double hoursWorked;

    private LocalDateTime createdAt;

    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "cityhall_id")
    private CityHall cityhallId;

}
