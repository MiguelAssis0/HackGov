package com.fiap.hackgov.shared.infra.services;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {

    public boolean isSameCityHall(Employee employee, CityHall cityHall) {
        return employee.getCityHallId().getId().equals(cityHall.getId());
    }

    public boolean hasAccessToSector(Employee employee, Sector sector) {
        return employee.getEmployeeJobLevels().stream().map(ejl -> ejl.getPk().getJobLevel()).flatMap(jl -> jl.getSectors().stream()).map(js -> js.getPk().getSector()).anyMatch(s -> s.getId().equals(sector.getId()));
    }

    public boolean hasPermission(Employee employee, String codename) {
        return employee.getEmployeeJobLevels().stream().map(ejl -> ejl.getPk().getJobLevel()).flatMap(jl -> jl.getPermissions().stream()).map(p -> p.getPk().getPermission()).anyMatch(p -> p.getCodename().equals(codename));
    }

    // 🔥 métodos de validação (os que você perguntou)
    public void checkCityHallAccess(Employee e, CityHall c) {
        if (!isSameCityHall(e, c)) {
            throw new BusinessException("Access denied");
        }
    }

    public void checkSectorAccess(Employee e, Sector s) {
        if (!hasAccessToSector(e, s)) {
            throw new BusinessException("No access to sector");
        }
    }

    public void checkPermission(Employee e, String permission) {
        if (!hasPermission(e, permission)) {
            throw new BusinessException("No permission: " + permission);
        }
    }
}
