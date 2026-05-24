package com.fiap.hackgov.cityhall_management.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation.CreateOccupationDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation.OccupationResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.services.OccupationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/occupations")
@AllArgsConstructor
public class OccupationController {

    private final OccupationService occupationService;

    @PostMapping
    @Operation(summary = "Create a new occupation", security = @SecurityRequirement(name = "bearer"), description = "Create a new occupation")
    public ResponseEntity<OccupationResponseDTO> createOccupation(@RequestBody @Valid CreateOccupationDTO dto, @AuthenticationPrincipal Employee employee) {
        OccupationResponseDTO response = occupationService.createOccupation(dto, employee);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all occupations", security = @SecurityRequirement(name = "bearer-key"), description = "Get all occupations")
    public ResponseEntity<Page<OccupationResponseDTO>> getAllOccupations(Pageable pageable, @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(occupationService.getAllOccupations(pageable, employee));
    }
}
