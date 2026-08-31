package com.fiap.hackgov.management.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.management.internal.DTOs.SectorPerformanceResponse;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagementMetricsService {
    private final SectorRepository sectorRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskReporitory taskRepository;

    @Transactional(readOnly = true)
    public List<SectorPerformanceResponse> find(Employee employee) {
        Employee current = require(employee);
        UUID cityId = current.getCityHallId().getId();
        List<Sector> sectors = sectorRepository.findAllByCityHall_Id(cityId, PageRequest.of(0, 500)).getContent();
        if (!Roles.ADMIN.equals(current.getRole())) {
            if (current.getSectorId() == null) return List.of();
            sectors = sectors.stream().filter(sector -> sector.getId().equals(current.getSectorId().getId())).toList();
        }
        List<Task> allTasks = taskRepository.findAllByBoard_CityHall_Id(cityId, PageRequest.of(0, 10_000)).getContent();
        return sectors.stream().map(sector -> metrics(sector, allTasks.stream()
                .filter(task -> task.getBoard() != null && task.getBoard().getSector() != null
                        && task.getBoard().getSector().getId().equals(sector.getId())).toList(), cityId)).toList();
    }

    private SectorPerformanceResponse metrics(Sector sector, List<Task> tasks, UUID cityId) {
        LocalDateTime now = LocalDateTime.now();
        long total = tasks.size();
        long completed = tasks.stream().filter(task -> task.getStatus() == Task.Status.COMPLETED).count();
        long overdue = tasks.stream().filter(task -> task.getStatus() != Task.Status.COMPLETED
                && task.getEndDate() != null && task.getEndDate().isBefore(now)).count();
        long active = total - completed;
        int completionRate = percent(completed, total);
        long completedOnTime = tasks.stream().filter(task -> task.getStatus() == Task.Status.COMPLETED
                && task.getCompletedAt() != null && (task.getEndDate() == null || !task.getCompletedAt().isAfter(task.getEndDate()))).count();
        int quality = percent(completedOnTime, completed);
        int productivity = total == 0 ? 0 : Math.max(0, Math.min(100, completionRate - (int) Math.round(overdue * 100.0 / total)));
        List<Integer> monthly = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = currentMonth.minusMonths(offset);
            long monthCreated = tasks.stream().filter(task -> task.getCreatedAt() != null && YearMonth.from(task.getCreatedAt()).equals(month)).count();
            long monthCompleted = tasks.stream().filter(task -> task.getCompletedAt() != null && YearMonth.from(task.getCompletedAt()).equals(month)).count();
            monthly.add(percent(monthCompleted, Math.max(monthCreated, monthCompleted)));
        }
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<Long> weekly = new ArrayList<>();
        for (int day = 0; day < 5; day++) {
            LocalDate date = monday.plusDays(day);
            weekly.add(tasks.stream().filter(task -> task.getCompletedAt() != null && task.getCompletedAt().toLocalDate().equals(date)).count());
        }
        double averageHours = tasks.stream().filter(task -> task.getCompletedAt() != null && task.getCreatedAt() != null)
                .mapToLong(task -> Math.max(0, Duration.between(task.getCreatedAt(), task.getCompletedAt()).toHours())).average().orElse(0);
        int trend = monthly.get(5) - monthly.get(0);
        return new SectorPerformanceResponse(sector.getId(), sector.getName(), productivity, completed, active, overdue,
                employeeRepository.countByCityHallId_IdAndSectorId_Id(cityId, sector.getId()), total, completionRate,
                trend, 80, quality, Math.round(averageHours * 10.0) / 10.0, monthly, weekly);
    }

    private int percent(long part, long total) {
        return total == 0 ? 0 : (int) Math.round(part * 100.0 / total);
    }

    private Employee require(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado");
        if (employee.getCityHallId() == null)
            throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        return employee;
    }
}
