package com.fiap.hackgov.auth.internal.controllers;


import com.fiap.hackgov.auth.internal.DTOs.users.CreateUserDTO;
import com.fiap.hackgov.auth.internal.DTOs.users.UserDTO;
import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.auth.internal.mapper.UserMapper;
import com.fiap.hackgov.auth.internal.services.UserService;
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

@RestController()
@RequestMapping("auth/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "Create User", description = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "user created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "user already exists")
    })
    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody @Valid CreateUserDTO createUserDTO) {
        User registeruser = userService.save(createUserDTO);
        URI address = URI.create("/auth/users/" + registeruser.getId());
        return ResponseEntity.created(address).build();
    }

    @Operation(
            summary = "Get All users",
            security = @SecurityRequirement(name = "bearer-key"),
            description = "Retrieve all users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        Page<UserDTO> userDTOs = userService.findAll(pageable)
                .map(userMapper::toUserDTO);
        return ResponseEntity.ok(userDTOs);

    }

    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearer-key"), description = "Retrieve an user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "user retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "user not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
