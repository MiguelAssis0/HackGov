package com.fiap.hackgov.mapper;


import com.fiap.hackgov.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.entities.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "cityhall", ignore = true)
    @Mapping(target = "password", ignore = true)
    Employee toEntity(CreateEmployeeDTO createEmployeeDTO);

    @Mapping(target = "cityHallId", source = "cityhall.id")
    @Mapping(target = "password", ignore = true)
    CreateEmployeeDTO toDTO(Employee employee);

    @Mapping(target = "cityHallId", source = "cityhall.id")
    EmployeeDTO toEmployeeDTO(Employee employee);
}
