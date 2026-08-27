package com.fiap.hackgov.dashboard.internal.DTOs;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class DashboardDTOs {
    private DashboardDTOs() {}

    public record Response(
            String userShortName,
            String cityHallName,
            String stateCode,
            Stats stats,
            List<Favorite> favorites,
            boolean agendaVisible,
            Calendar calendar,
            List<UpcomingEvent> upcomingEvents,
            List<TaskItem> tasks
    ) {}

    public record Stats(long employees, long sectors, long occupations, long favorites) {}

    public record Favorite(String slug, String title, String route, String icon, String typeLabel) {}

    public record Calendar(int year, int month, String label, List<CalendarDay> days) {}

    public record CalendarDay(LocalDate date, boolean inMonth, boolean today, int itemCount) {}

    public record UpcomingEvent(
            UUID id,
            String title,
            LocalDate startDate,
            LocalTime startTime,
            String icon,
            boolean linkedTask
    ) {}

    public record TaskItem(
            UUID id,
            String title,
            LocalDate deadline,
            String deadlineState,
            String deadlineLabel,
            String statusLabel,
            String priorityLabel
    ) {}
}
