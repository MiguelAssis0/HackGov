package com.fiap.hackgov.cityhall_management.internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.RequisitionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employees")
@Filter(name = "cityHallFilter", condition = "city_hall_id = :cityHallId")
@Filter(name = "sectorFilter", condition = "sector_id = :sectorId")
public class Employee extends User implements Serializable {

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

    @ManyToOne
    @JoinColumn(name = "sector_id")
    @JsonIgnore
    private Sector sectorId;

    @ManyToOne
    private Occupation occupationId;


    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    }

    @Override
    public String toString() {
        return "Employee [id=" + this.getId() + ", name=" + this.getFullName() + "]";
    }

}
