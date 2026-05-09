package com.fiap.hackgov.cityhall_management.internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.RequisitionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

    @OneToMany(mappedBy = "pk.employee", cascade = CascadeType.ALL)
    @JsonIgnore
    private final List<EmployeeJobLevel> employeeJobLevels = new ArrayList<>();

    private Double salary;

    private LocalDateTime admissionDate;

    private RequisitionStatus requisitionStatus;

    private LocalDateTime dismissalDate;

    private String registrationNumber;

    private Double hoursWorked;

    @ManyToOne
    @JoinColumn(name = "cityHall_id")
    @JsonIgnore
    private CityHall cityHallId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return this.getEmployeeJobLevels().stream()
                .map(ejl -> ejl.getPk().getJobLevel())
                .flatMap(jl -> jl.getPermissions().stream())
                .map(pjl -> pjl.getPk().getPermission())
                .map(permission -> new SimpleGrantedAuthority(permission.getCodename()))
                .distinct()
                .toList();
    }

}
