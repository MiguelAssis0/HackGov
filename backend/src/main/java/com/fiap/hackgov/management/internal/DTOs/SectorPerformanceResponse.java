package com.fiap.hackgov.management.internal.DTOs;

import java.util.List;
import java.util.UUID;

public record SectorPerformanceResponse(
        UUID id, String name, int productivity, long completed, long activeTasks, long overdue,
        long employees, long totalTasks, int completionRate, int trend, int goal, int quality,
        double averageResponseHours, List<Integer> monthly, List<Long> weekly
) {}
