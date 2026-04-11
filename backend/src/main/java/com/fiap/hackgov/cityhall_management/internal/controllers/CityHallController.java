package com.fiap.hackgov.cityhall_management.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.DTOs.CityHall.CityHallDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.CityHall.CreateCityHallDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.mapper.CityHallMapper;
import com.fiap.hackgov.cityhall_management.internal.services.CityHallService;
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

@RestController
@RequestMapping("/api/cityhall")
public class CityHallController {

    @Autowired
    private CityHallService cityHallService;

    @Autowired
    private CityHallMapper cityHallMapper;

    @Operation(summary = "Create CityHall", description = "Create a new city hall")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CityHall created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "State not found"),
            @ApiResponse(responseCode = "409", description = "CityHall already exists")
    })
    @PostMapping
    public ResponseEntity<Void> createCityHall(@RequestBody @Valid CreateCityHallDTO cityHallDTO) {
        CityHall createdCityHall = cityHallService.save(cityHallDTO);
        URI address = URI.create("/api/cityhall/" + createdCityHall.getId());
        return ResponseEntity.created(address).build();
    }

    @Operation(
            summary = "Get All CityHalls",
            security = @SecurityRequirement(name = "bearer-key"),
            description = "Retrieve all city halls"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CityHalls retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<Page<CityHallDTO>> getAllCityHalls(Pageable pageable) {
        Page<CityHallDTO> cityHallDTOs = cityHallService.findAll(pageable)
                .map(cityHallMapper::toCityHallDTO);
        return ResponseEntity.ok(cityHallDTOs);
    }

    @Operation(
            summary = "Get CityHall by ID",
            security = @SecurityRequirement(name = "bearer-key"),
            description = "Retrieve a city hall by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CityHall retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "CityHall not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CityHallDTO> getCityHallById(@PathVariable UUID id) {
        return ResponseEntity.ok(cityHallMapper.toCityHallDTO(cityHallService.findById(id)));
    }
}