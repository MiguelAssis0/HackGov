package com.fiap.hackgov.shared.infra.services;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {

    public boolean verifySameCityHall(Employee employee, CityHall cityHall){
        return employee.getCityHallId().getId() == cityHall.getId();
    }

    public boolean verifySameSector(Employee employee, CityHall cityHall, Sector sector) {

        if (!employee.getCityHallId().getId().equals(cityHall.getId())) {
            return false;
        }

        return employee.getEmployeeJobLevels().stream()
                .map(ejl -> ejl.getPk().getJobLevel())
                .flatMap(jl -> jl.getSectors().stream())
                .anyMatch(jls -> jls.getPk().getSector().getId().equals(sector.getId()));
    }
}
