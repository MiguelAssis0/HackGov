package com.fiap.hackgov.cityhall_management.internal.mapper;


import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.EmployeeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "cityHallId", ignore = true)
    @Mapping(target = "sectorId", ignore = true)
    @Mapping(target = "occupationId", ignore = true)
    @Mapping(target = "role", ignore = true)
    Employee toEntity(CreateEmployeeDTO createEmployeeDTO);

    @Mapping(target = "cityHallId", source = "cityHallId.id")
    EmployeeDTO toEmployeeDTO(Employee employee);

    default CityHall map(UUID id) {
        if (id == null) return null;
        CityHall cityHall = new CityHall();
        cityHall.setId(id);
        return cityHall;
    }

    default Sector mapSector(UUID id) {
        if (id == null) return null;
        Sector sector = new Sector();
        sector.setId(id);
        return sector;
    }

    default Occupation mapOccupation(UUID id) {
        if (id == null) return null;
        Occupation occupation = new Occupation();
        occupation.setId(id);
        return occupation;
    }
}
