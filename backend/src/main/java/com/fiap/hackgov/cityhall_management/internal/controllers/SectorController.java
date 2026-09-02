package com.fiap.hackgov.cityhall_management.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.CreateSectorDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.SectorResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.services.SectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/sectors")
@AllArgsConstructor
public class SectorController {

    private final SectorService sectorService;

    @PostMapping
    @Operation(summary = "Create a new sector", security = @SecurityRequirement(name = "bearer"), description = "Create a new sector")
    public ResponseEntity<SectorResponseDTO> createSector(@RequestBody @Valid CreateSectorDTO createSectorDTO, @AuthenticationPrincipal Employee employee) {
        SectorResponseDTO response = sectorService.createSector(createSectorDTO, employee);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all sectors", security = @SecurityRequirement(name = "bearer-key"), description = "Get all sectors")
    public ResponseEntity<Page<SectorResponseDTO>> getAllSectors(Pageable pageable, @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(sectorService.getAllSectors(pageable, employee));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a sector by id", security = @SecurityRequirement(name = "bearer"), description = "Get a sector by id")
    public ResponseEntity<SectorResponseDTO> getById(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(sectorService.getById(id, employee));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get a sector by name", security = @SecurityRequirement(name = "bearer"), description = "Get a sector by name")
    public ResponseEntity<SectorResponseDTO> getByName(@PathVariable String name, @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(sectorService.getByName(name, employee));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a sector", security = @SecurityRequirement(name = "bearer"))
    public ResponseEntity<SectorResponseDTO> updateSector(@PathVariable UUID id, @RequestBody @Valid CreateSectorDTO dto, @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(sectorService.updateSector(id, dto, employee));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle sector status", security = @SecurityRequirement(name = "bearer"))
    public ResponseEntity<SectorResponseDTO> toggleSector(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(sectorService.toggleSector(id, employee));
    }
}
