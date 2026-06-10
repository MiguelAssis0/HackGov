package com.fiap.hackgov.shared.infra.config.mocks.employee;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EmployeeFactory {
    private final PasswordEncoder passwordEncoder;

    public Employee create(String firstName, String lastName, String email, String cpf, String registration, Double salary, Occupation occupation, CityHall cityHall) {
        return create(firstName, lastName, email, cpf, registration, salary, occupation, occupation.getSectorId(), cityHall, Roles.EMPLOYEE);
    }

    public Employee create(String firstName, String lastName, String email, String cpf, String registration, Double salary, Occupation occupation, Sector sector, CityHall cityHall, Roles role) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setCpf(cpf);
        employee.setPassword(passwordEncoder.encode("senha123"));
        employee.setStatus(true);
        employee.setRole(role);
        employee.setTwoFactor(false);
        employee.setSalary(salary);
        employee.setRegistrationNumber(registration);
        employee.setAdmissionDate(LocalDateTime.now().minusYears(2));
        employee.setHoursWorked(1200.0);
        employee.setOccupationId(occupation);
        employee.setSectorId(sector);
        employee.setCityHallId(cityHall);
        return employee;
    }
}
