package com.fiap.hackgov.cityhall_management.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.mapper.EmployeeMapper;
import com.fiap.hackgov.cityhall_management.internal.services.EmployeeService;
import com.fiap.hackgov.shared.infra.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<Void> createEmployee(@RequestBody @Valid CreateEmployeeDTO employeeDTO) {
        Employee registerEmployee = employeeService.save(employeeDTO);
        URI address = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registerEmployee.getId())
                .toUri();
        return ResponseEntity.created(address).build();
    }

    @Operation(
            summary = "Get All Employees",
            security = @SecurityRequirement(name = "bearer-key"),
            description = "Retrieve all employees"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(Pageable pageable) {
            Page<EmployeeDTO> employeeDTOs = employeeService.findAll(pageable)
                    .map(employeeMapper::toEmployeeDTO);
            return ResponseEntity.ok(employeeDTOs);

    }

    @Operation(summary = "Get Employee by ID", security = @SecurityRequirement(name = "bearer-key"), description = "Retrieve an employee by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeMapper.toEmployeeDTO(employeeService.findById(id)));
    }




}
