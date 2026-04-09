package com.fiap.hackgov.erp.internal.mapper;


import com.fiap.hackgov.erp.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.erp.internal.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.erp.internal.entities.CityHall;
import com.fiap.hackgov.erp.internal.entities.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "cityhallId", ignore = true)  // ✅ era "cityhall", corrigido
    Employee toEntity(CreateEmployeeDTO createEmployeeDTO);

    @Mapping(target = "cityhallId", source = "cityhallId.id")  // ✅ corrigido
    CreateEmployeeDTO toDTO(Employee employee);

    @Mapping(target = "cityhallId", source = "cityhallId.id")  // ✅ corrigido
    EmployeeDTO toEmployeeDTO(Employee employee);

    default CityHall map(UUID id) {
        if (id == null) return null;
        CityHall cityHall = new CityHall();
        cityHall.setId(id);
        return cityHall;
    }
}