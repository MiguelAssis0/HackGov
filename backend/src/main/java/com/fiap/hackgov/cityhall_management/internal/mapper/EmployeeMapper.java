package com.fiap.hackgov.cityhall_management.internal.mapper;


import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.EmployeeDetailsResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.EmployeeResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "cityHallId", ignore = true)
    @Mapping(target = "sectorId", ignore = true)
    @Mapping(target = "occupationId", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "accessibility", ignore = true)
    @Mapping(target = "avatarPath", ignore = true)
    @Mapping(target = "twoFactor", ignore = true)
    @Mapping(target = "acceptTerms", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "requisitionStatus", ignore = true)
    @Mapping(target = "dismissalDate", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    Employee toEntity(CreateEmployeeDTO createEmployeeDTO);

    @Mapping(target = "cityHallId", source = "cityHallId.id")
    @Mapping(target = "occupationName", source = "occupationId.name")
    @Mapping(target = "sectorName", source = "sectorId.name")
    EmployeeResponseDTO toEmployeeDTO(Employee employee);

    @Mapping(target = "name", expression = "java(employee.getFullName())")
    @Mapping(target = "cityhall", source = "cityHallId.name")
    @Mapping(target = "occupation", source = "occupationId.name")
    @Mapping(target = "sector", source = "sectorId.name")
    EmployeeDetailsResponseDTO toEmployeeDetailsResponseDTO(Employee employee);

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
