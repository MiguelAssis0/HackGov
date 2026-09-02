package com.fiap.hackgov.cityhall_management.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.EmployeeDetailsResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.EmployeeResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.UpdateEmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.mapper.EmployeeMapper;
import com.fiap.hackgov.cityhall_management.internal.services.EmployeeService;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping
    public ResponseEntity<Void> createEmployee(@RequestBody @Valid CreateEmployeeDTO employeeDTO) {
        Employee registerEmployee = employeeService.save(employeeDTO);
        URI address = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(registerEmployee.getId()).toUri();
        return ResponseEntity.created(address).build();
    }

    @Operation(summary = "Get All Employees", security = @SecurityRequirement(name = "bearer-key"), description = "Retrieve all employees")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Employees retrieved successfully"), @ApiResponse(responseCode = "401", description = "Unauthorized")})
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllEmployees(Pageable pageable, @AuthenticationPrincipal Employee employee) {
        if (employee == null) {
            throw new UnauthorizedException("Is necessary to be authenticated to list the users");
        }

        Page<EmployeeResponseDTO> employeeDTOs = employeeService.findAll(pageable, employee).map(employeeMapper::toEmployeeDTO);
        return ResponseEntity.ok(employeeDTOs);

    }

    @Operation(summary = "Get Employee by ID", security = @SecurityRequirement(name = "bearer-key"), description = "Retrieve an employee by their ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Employee retrieved successfully"), @ApiResponse(responseCode = "404", description = "Employee not found"), @ApiResponse(responseCode = "401", description = "Unauthorized")})
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeMapper.toEmployeeDTO(employeeService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(@PathVariable UUID id, @RequestBody UpdateEmployeeDTO dto, @AuthenticationPrincipal Employee actor) {
        Employee updated = employeeService.update(id, dto, actor);
        return ResponseEntity.ok(employeeMapper.toEmployeeDTO(updated));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<EmployeeResponseDTO> toggle(@PathVariable UUID id, @AuthenticationPrincipal Employee actor) {
        Employee updated = employeeService.toggle(id, actor);
        return ResponseEntity.ok(employeeMapper.toEmployeeDTO(updated));
    }

    @Operation(summary = "Authenticated employee details", description = "Return authenticated employee details", security = @SecurityRequirement(name = "bearer-key"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Details returned successfully"), @ApiResponse(responseCode = "401", description = "Unauthorized")})
    @GetMapping("/details")
    public ResponseEntity<EmployeeDetailsResponseDTO> details(@AuthenticationPrincipal Employee employee) {
        if (employee == null) {
            throw new UnauthorizedException("E necessario estar autenticado para acessar os detalhes do usuario");
        }
        EmployeeDetailsResponseDTO response = employeeService.getEmployeeDetails(employee);
        return ResponseEntity.ok(response);
    }

}
