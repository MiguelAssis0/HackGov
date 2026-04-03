package com.fiap.hackgov.controllers;

import com.fiap.hackgov.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.entities.User;
import com.fiap.hackgov.infra.security.TokenService;
import com.fiap.hackgov.mapper.EmployeeMapper;
import com.fiap.hackgov.services.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Operation(summary = "Create Employee", description = "Create a new employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Employee already exists")
    })
    @PostMapping
    public ResponseEntity<Void> createEmployee(@RequestBody @Valid CreateEmployeeDTO employeeDTO) {
        Employee registerEmployee = employeeService.save(employeeDTO);
        URI address = URI.create("/api/employee/" + registerEmployee.getId());
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
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(HttpServletRequest request, Pageable pageable) {
        Page<Employee> employees = employeeService.findAll(request, pageable);
        Page<EmployeeDTO> employeeDTOs = employees.map(employeeMapper::toEmployeeDTO);
        return ResponseEntity.ok(employeeDTOs);
    }

    @Operation(summary = "Get Employee by ID", security = @SecurityRequirement(name = "bearer-key"), description = "Retrieve an employee by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable UUID id, HttpServletRequest request) {
        EmployeeDTO employee = employeeService.findById(id.toString(), request);
        return ResponseEntity.ok(employee);
    }




}
