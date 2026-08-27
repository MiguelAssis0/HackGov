package com.fiap.hackgov.dashboard.internal.services;

import com.fiap.hackgov.agenda.internal.entities.AgendaEvent;
import com.fiap.hackgov.agenda.internal.repositories.AgendaEventRepository;
import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.OccupationRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.dashboard.internal.DTOs.DashboardDTOs;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import com.fiap.hackgov.tools.internal.services.ToolConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final EmployeeRepository employeeRepository;
    private final SectorRepository sectorRepository;
    private final OccupationRepository occupationRepository;
    private final TaskReporitory taskRepository;
    private final AgendaEventRepository agendaEventRepository;
    private final ToolConfigurationService toolConfigurationService;

    @Transactional
    public DashboardDTOs.Response get(Employee principal) {
        Employee employee = requireEmployee(principal);
        CityHall cityHall = employee.getCityHallId();
        UUID cityHallId = cityHall.getId();
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        YearMonth currentMonth = YearMonth.from(today);

        List<ToolConfigurationService.Response> visibleTools = toolConfigurationService.list(employee);
        List<DashboardDTOs.Favorite> favorites = visibleTools.stream()
                .filter(ToolConfigurationService.Response::favorite)
                .filter(tool -> tool.enabled() || tool.mandatory())
                .filter(tool -> tool.route() != null && !tool.route().isBlank())
                .map(tool -> new DashboardDTOs.Favorite(
                        tool.id(), tool.name(), tool.route(), tool.icon(), tool.category()
                ))
                .toList();

        boolean agendaVisible = visibleTools.stream()
                .anyMatch(tool -> "agenda".equals(tool.id()) && (tool.enabled() || tool.mandatory()));

        List<Task> assignedTasks = taskRepository.findDashboardAssignedTasks(
                        cityHallId, employee.getId(), Task.Status.COMPLETED
                ).stream()
                .sorted(Comparator
                        .comparing(Task::getEndDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(task -> priorityRank(task.getPriority()))
                        .thenComparing(Task::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();

        List<Task> calendarTasks = agendaVisible ? calendarTasks(employee, currentMonth) : List.of();
        List<AgendaEvent> monthEvents = agendaVisible
                ? agendaEventRepository.findMonth(cityHallId, currentMonth.atDay(1), currentMonth.atEndOfMonth(), null)
                : List.of();
        List<AgendaEvent> upcomingEvents = agendaVisible
                ? agendaEventRepository.findUpcoming(cityHallId, today).stream().limit(3).toList()
                : List.of();

        DashboardDTOs.Stats stats = new DashboardDTOs.Stats(
                employeeRepository.countByCityHallId_Id(cityHallId),
                sectorRepository.countByCityHall_Id(cityHallId),
                occupationRepository.countBySectorId_CityHall_Id(cityHallId),
                favorites.size()
        );

        String stateCode = cityHall.getState() == null || cityHall.getState().getUf() == null
                ? ""
                : cityHall.getState().getUf().name();

        return new DashboardDTOs.Response(
                employee.getFirstName(), cityHall.getName(), stateCode, stats, favorites,
                agendaVisible, calendar(currentMonth, today, monthEvents, calendarTasks),
                upcomingEvents.stream().map(this::upcoming).toList(),
                assignedTasks.stream().map(task -> task(task, today)).toList()
        );
    }

    private Employee requireEmployee(Employee principal) {
        if (principal == null) {
            throw new UnauthorizedException("E necessario estar autenticado para acessar o dashboard");
        }
        Employee employee = employeeRepository.findByIdWithDetails(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("Usuario autenticado nao encontrado"));
        if (employee.getCityHallId() == null) {
            throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        }
        return employee;
    }

    private List<Task> calendarTasks(Employee employee, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime endExclusive = month.plusMonths(1).atDay(1).atStartOfDay();
        UUID cityHallId = employee.getCityHallId().getId();
        if (Roles.ADMIN.equals(employee.getRole())) {
            return taskRepository.findDashboardCalendarTasks(
                    cityHallId, Task.Status.COMPLETED, start, endExclusive
            );
        }
        if (employee.getSectorId() == null) return List.of();
        return taskRepository.findDashboardCalendarTasksForSector(
                cityHallId, employee.getSectorId().getId(), Task.Status.COMPLETED, start, endExclusive
        );
    }

    private DashboardDTOs.Calendar calendar(
            YearMonth month,
            LocalDate today,
            List<AgendaEvent> events,
            List<Task> tasks
    ) {
        LocalDate first = month.atDay(1);
        LocalDate last = month.atEndOfMonth();
        LocalDate gridStart = first.minusDays(daysSinceSunday(first.getDayOfWeek()));
        LocalDate gridEnd = last.plusDays(6 - daysSinceSunday(last.getDayOfWeek()));
        List<DashboardDTOs.CalendarDay> days = gridStart.datesUntil(gridEnd.plusDays(1))
                .map(date -> new DashboardDTOs.CalendarDay(
                        date,
                        YearMonth.from(date).equals(month),
                        date.equals(today),
                        itemCount(date, events, tasks)
                ))
                .toList();
        String monthName = month.getMonth().getDisplayName(TextStyle.FULL, PT_BR);
        String label = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1) + " de " + month.getYear();
        return new DashboardDTOs.Calendar(month.getYear(), month.getMonthValue(), label, days);
    }

    private int itemCount(LocalDate date, List<AgendaEvent> events, List<Task> tasks) {
        long eventCount = events.stream()
                .filter(event -> !date.isBefore(event.getStartDate()) && !date.isAfter(event.effectiveEndDate()))
                .count();
        long taskCount = tasks.stream()
                .filter(task -> task.getEndDate() != null && task.getEndDate().toLocalDate().equals(date))
                .count();
        return Math.toIntExact(eventCount + taskCount);
    }

    private int daysSinceSunday(DayOfWeek day) {
        return day.getValue() % 7;
    }

    private DashboardDTOs.UpcomingEvent upcoming(AgendaEvent event) {
        return new DashboardDTOs.UpcomingEvent(
                event.getId(), event.getTitle(), event.getStartDate(), event.getStartTime(),
                eventIcon(event.getType()), event.getTask() != null
        );
    }

    private String eventIcon(AgendaEvent.Type type) {
        return switch (type) {
            case MEETING -> "bi-people-fill";
            case DEADLINE -> "bi-hourglass-split";
            case SERVICE -> "bi-person-workspace";
            case CEREMONY -> "bi-bank2";
            case OTHER -> "bi-calendar-event-fill";
        };
    }

    private DashboardDTOs.TaskItem task(Task task, LocalDate today) {
        LocalDate deadline = task.getEndDate() == null ? null : task.getEndDate().toLocalDate();
        String deadlineState;
        String deadlineLabel;
        if (deadline == null) {
            deadlineState = "sem_prazo";
            deadlineLabel = "Prazo de entrega não definido";
        } else if (deadline.isBefore(today)) {
            deadlineState = "atrasada";
            deadlineLabel = "Atrasada";
        } else if (deadline.equals(today)) {
            deadlineState = "hoje";
            deadlineLabel = "Vence hoje";
        } else if (!deadline.isAfter(today.plusDays(3))) {
            deadlineState = "proxima";
            deadlineLabel = "Próxima do vencimento";
        } else {
            deadlineState = "futura";
            deadlineLabel = "No prazo";
        }
        return new DashboardDTOs.TaskItem(
                task.getId(), task.getTitle(), deadline, deadlineState, deadlineLabel,
                statusLabel(task.getStatus()), priorityLabel(task.getPriority())
        );
    }

    private int priorityRank(Task.Priority priority) {
        return switch (priority == null ? Task.Priority.NORMAL : priority) {
            case URGENT -> 0;
            case HIGH -> 1;
            case NORMAL -> 2;
            case LOW -> 3;
        };
    }

    private String statusLabel(Task.Status status) {
        return switch (status == null ? Task.Status.TODO : status) {
            case TODO -> "A fazer";
            case IN_PROGRESS -> "Em andamento";
            case IN_REVIEW -> "Em revisão";
            case COMPLETED -> "Concluída";
        };
    }

    private String priorityLabel(Task.Priority priority) {
        return switch (priority == null ? Task.Priority.NORMAL : priority) {
            case LOW -> "Baixa";
            case NORMAL -> "Normal";
            case HIGH -> "Alta";
            case URGENT -> "Urgente";
        };
    }
}
