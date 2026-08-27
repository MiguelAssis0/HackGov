package com.fiap.hackgov.dashboard.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.dashboard.internal.DTOs.DashboardDTOs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class DashboardServiceIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void returnsTenantSummaryCurrentCalendarAndOnlyAssignedOpenTasks() {
        Employee admin = employeeRepository.findByEmail("admin@admin.com").orElseThrow();

        DashboardDTOs.Response response = dashboardService.get(admin);

        assertThat(response.cityHallName()).isNotBlank();
        assertThat(response.userShortName()).isEqualTo(admin.getFirstName());
        assertThat(response.stats().employees()).isPositive();
        assertThat(response.calendar().days()).hasSizeBetween(35, 42);
        assertThat(response.calendar().days()).anyMatch(DashboardDTOs.CalendarDay::today);
        assertThat(response.tasks()).hasSizeLessThanOrEqualTo(5)
                .allMatch(task -> task.statusLabel() != null && !task.statusLabel().equals("Concluída"));
    }
}
