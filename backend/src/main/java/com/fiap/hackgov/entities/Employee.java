package com.fiap.hackgov.entities;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employees")
public class Employee extends User {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return "";
    }

    private JobLevel jobLevel;
    @CPF
    private String cpf;
    private Double salary;
    private LocalDateTime admissionDate;
    private LocalDateTime dismissalDate;
    private String registrationNumber;
    private Double hoursWorked;
    private LocalDateTime createdAt;

}
