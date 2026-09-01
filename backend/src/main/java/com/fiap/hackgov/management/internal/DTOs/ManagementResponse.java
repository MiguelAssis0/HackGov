package com.fiap.hackgov.management.internal.DTOs;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ManagementResponse(
        String cityHallName, boolean filtersValid, List<String> filterErrors,
        Period period, UUID selectedSectorId, boolean restrictedScope,
        List<SectorOption> sectorOptions, Indicators indicators,
        List<EmployeePerformance> employees, List<SectorPerformance> sectors,
        List<TemporalPoint> temporalSeries, List<EmployeeComparison> comparison,
        long tasksWithoutResponsible
) {
    public record Period(String value, String label, LocalDate start, LocalDate end) {}
    public record SectorOption(UUID id, String name) {}
    public record Indicators(long totalTasks, long totalPoints, double averageTasksPerEmployee,
                             double averagePointsPerEmployee, double averagePointsPerTask,
                             long employeesWithDeliveries) {}
    public record EmployeePerformance(UUID id, String name, String sector, long tasks, long points,
                                      double averagePoints, double participation, List<Long> evolution,
                                      int position) {}
    public record SectorPerformance(UUID id, String name, long tasks, long points, long employees,
                                    double averageTasks, double averagePoints, double participation) {}
    public record TemporalPoint(String label, long value) {}
    public record EmployeeComparison(String name, String sector, long tasks, long points) {}
}
