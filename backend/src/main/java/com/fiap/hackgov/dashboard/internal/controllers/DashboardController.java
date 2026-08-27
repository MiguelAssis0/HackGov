package com.fiap.hackgov.dashboard.internal.controllers;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.dashboard.internal.DTOs.DashboardDTOs;
import com.fiap.hackgov.dashboard.internal.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public DashboardDTOs.Response get(@AuthenticationPrincipal Employee employee) {
        return dashboardService.get(employee);
    }
}
