package com.fiap.hackgov.management.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.management.internal.DTOs.ManagementResponse;
import com.fiap.hackgov.management.internal.services.ManagementMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/management")
@RequiredArgsConstructor
public class ManagementMetricsController {
    private final ManagementMetricsService service;

    @GetMapping
    public ManagementResponse management(@RequestParam(defaultValue = "mes") String period,
                                          @RequestParam(required = false) UUID sectorId,
                                          @RequestParam(required = false) LocalDate start,
                                          @RequestParam(required = false) LocalDate end,
                                          @AuthenticationPrincipal Employee employee) {
        return service.find(employee, period, sectorId, start, end);
    }
}
