package com.fiap.hackgov.auth.internal.controllers;


import com.fiap.hackgov.auth.internal.DTOs.users.CreateUserDTO;
import com.fiap.hackgov.auth.internal.DTOs.users.UserDTO;
import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.auth.internal.mapper.UserMapper;
import com.fiap.hackgov.auth.internal.services.UserService;
import com.fiap.hackgov.erp.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.erp.internal.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.erp.internal.entities.Employee;
import com.fiap.hackgov.erp.internal.mapper.EmployeeMapper;
import com.fiap.hackgov.erp.internal.services.EmployeeService;
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

import java.net.URI;
import java.util.UUID;

@RestController("/auth/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "Create Employee", description = "Create a new employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Employee already exists")
    })
    @PostMapping
    public ResponseEntity<Void> createEmployee(@RequestBody @Valid CreateUserDTO createUserDTO) {
        User registerEmployee = userService.save(createUserDTO);
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
    public ResponseEntity<Page<UserDTO>> getAllEmployees(Pageable pageable) {
        Page<UserDTO> userDTOs = userService.findAll(pageable)
                .map(userMapper::toUserDTO);
        return ResponseEntity.ok(userDTOs);

    }

    @Operation(summary = "Get Employee by ID", security = @SecurityRequirement(name = "bearer-key"), description = "Retrieve an employee by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getEmployeeById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
