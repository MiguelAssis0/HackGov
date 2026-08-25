package com.fiap.hackgov.management.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.management.internal.DTOs.SectorPerformanceResponse;
import com.fiap.hackgov.management.internal.services.ManagementMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/management")
@RequiredArgsConstructor
public class ManagementMetricsController {
    private final ManagementMetricsService service;

    @GetMapping("/sector-performance")
    public List<SectorPerformanceResponse> sectorPerformance(@AuthenticationPrincipal Employee employee) {
        return service.find(employee);
    }
}
