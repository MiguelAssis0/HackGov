package com.fiap.hackgov.entities;

import com.fiap.hackgov.entities.enums.RequisitionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employees")
public class Employee extends User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.getEmail();
    }

    @OneToMany(mappedBy = "pk.employee", cascade = CascadeType.ALL)
    private final List<EmployeeJobLevel> employeeJobLevels = new ArrayList<>();

    private Double salary;

    private LocalDateTime admissionDate;

    private RequisitionStatus requisitionStatus;

    private LocalDateTime dismissalDate;

    private String registrationNumber;

    private Double hoursWorked;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "cityhall_id")
    private CityHall cityhall;

}
