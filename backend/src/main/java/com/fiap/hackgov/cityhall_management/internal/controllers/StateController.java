package com.fiap.hackgov.cityhall_management.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.DTOs.State.StateDTO;
import com.fiap.hackgov.cityhall_management.internal.mapper.StateMapper;
import com.fiap.hackgov.cityhall_management.internal.services.StateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/state")
public class StateController {

    @Autowired
    private StateService stateService;

    @Autowired
    private StateMapper stateMapper;

    @Operation(
            summary = "Get All States",
            security = @SecurityRequirement(name = "bearer-key"),
            description = "Retrieve all states"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "States retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<Page<StateDTO>> getAllStates(Pageable pageable) {
        Page<StateDTO> stateDTOs = stateService.findAll(pageable)
                .map(stateMapper::toStateDTO);
        return ResponseEntity.ok(stateDTOs);
    }

    @Operation(
            summary = "Get State by ID",
            security = @SecurityRequirement(name = "bearer-key"),
            description = "Retrieve a state by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "State retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "State not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StateDTO> getStateById(@PathVariable UUID id) {
        return ResponseEntity.ok(stateMapper.toStateDTO(stateService.findById(id)));
    }
}