package com.fiap.hackgov.management.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.CityHallRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.management.internal.DTOs.ManagementResponse;
import com.fiap.hackgov.management.internal.DTOs.ManagementResponse.EmployeeComparison;
import com.fiap.hackgov.management.internal.DTOs.ManagementResponse.EmployeePerformance;
import com.fiap.hackgov.management.internal.DTOs.ManagementResponse.Indicators;
import com.fiap.hackgov.management.internal.DTOs.ManagementResponse.Period;
import com.fiap.hackgov.management.internal.DTOs.ManagementResponse.SectorOption;
import com.fiap.hackgov.management.internal.DTOs.ManagementResponse.SectorPerformance;
import com.fiap.hackgov.management.internal.DTOs.ManagementResponse.TemporalPoint;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import com.fiap.hackgov.tools.internal.services.ToolPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagementMetricsService {
    private static final String NO_SECTOR = "Sem setor";
    private static final Map<String, String> PERIOD_LABELS = Map.of(
            "semana", "Semana", "mes", "Mês", "ano", "Ano", "personalizado", "Período personalizado");

    private final SectorRepository sectorRepository;
    private final EmployeeRepository employeeRepository;
    private final CityHallRepository cityHallRepository;
    private final TaskReporitory taskRepository;
    private final ToolPermissionService toolPermissionService;

    @Transactional(readOnly = true)
    public ManagementResponse find(Employee employee, String periodValue, UUID sectorId,
                                   LocalDate customStart, LocalDate customEnd) {
        Employee current = require(employee);
        UUID cityId = current.getCityHallId().getId();
        boolean admin = Roles.ADMIN.equals(current.getRole());
        if (!admin && !toolPermissionService.canAccess("relatorios", current)) {
            throw new UnauthorizedException("Voce nao possui acesso aos relatorios");
        }

        List<Sector> visibleSectors = sectorRepository.findAllByCityHall_IdAndActiveTrueOrderByNameAsc(cityId);
        if (!admin) {
            if (current.getSectorId() == null) visibleSectors = List.of();
            else visibleSectors = visibleSectors.stream().filter(item -> item.getId().equals(current.getSectorId().getId())).toList();
        }
        List<String> errors = new ArrayList<>();
        String normalizedPeriod = PERIOD_LABELS.containsKey(periodValue) ? periodValue : "mes";
        if (periodValue != null && !PERIOD_LABELS.containsKey(periodValue)) errors.add("Período inválido.");
        if ("personalizado".equals(normalizedPeriod) && (customStart == null || customEnd == null)) {
            errors.add("Informe a data inicial e a data final.");
        }
        if ("personalizado".equals(normalizedPeriod) && customStart != null && customEnd != null && customEnd.isBefore(customStart)) {
            errors.add("A data final não pode ser anterior à inicial.");
        }
        UUID requestedSectorId = sectorId;
        boolean validSector = requestedSectorId == null || visibleSectors.stream().anyMatch(item -> item.getId().equals(requestedSectorId));
        if (!validSector) {
            errors.add("Setor inválido para o usuário atual.");
        }
        UUID selectedSectorId = validSector ? requestedSectorId : null;
        if (!errors.isEmpty()) normalizedPeriod = "mes";

        LocalDate today = LocalDate.now();
        LocalDate start = switch (normalizedPeriod) {
            case "semana" -> today.minusDays(today.getDayOfWeek().getValue() - 1L);
            case "ano" -> LocalDate.of(today.getYear(), 1, 1);
            case "personalizado" -> customStart;
            default -> today.withDayOfMonth(1);
        };
        LocalDate end = switch (normalizedPeriod) {
            case "semana" -> start.plusDays(6);
            case "ano" -> LocalDate.of(today.getYear(), 12, 31);
            case "personalizado" -> customEnd;
            default -> start.with(TemporalAdjusters.lastDayOfMonth());
        };
        List<Task> tasks = taskRepository.findCompletedForManagement(cityId, Task.Status.COMPLETED,
                start.atStartOfDay(), end.plusDays(1).atStartOfDay(), selectedSectorId);
        if (selectedSectorId != null) visibleSectors = visibleSectors.stream().filter(item -> item.getId().equals(selectedSectorId)).toList();

        Map<UUID, Employee> employees = employeeRepository.findAllByCityHallId_IdAndStatusTrueOrderByFirstNameAscLastNameAsc(cityId)
                .stream().filter(item -> selectedSectorId == null || item.getSectorId() != null && item.getSectorId().getId().equals(selectedSectorId))
                .collect(Collectors.toMap(Employee::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        tasks.stream().flatMap(task -> responsibles(task).stream()).forEach(item -> employees.putIfAbsent(item.getId(), item));

        List<TemporalPoint> series = temporalSeries(tasks, start, end);
        Map<UUID, List<Task>> byEmployee = new HashMap<>();
        tasks.forEach(task -> responsibles(task).forEach(item -> byEmployee.computeIfAbsent(item.getId(), ignored -> new ArrayList<>()).add(task)));
        List<EmployeePerformance> employeeMetrics = employees.values().stream()
                .map(item -> employeeMetric(item, byEmployee.getOrDefault(item.getId(), List.of()), tasks.size(), start, end))
                .sorted(Comparator.comparingLong(EmployeePerformance::points).reversed()
                        .thenComparing(Comparator.comparingLong(EmployeePerformance::tasks).reversed())
                        .thenComparing(EmployeePerformance::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<EmployeePerformance> ranked = new ArrayList<>();
        for (int index = 0; index < employeeMetrics.size(); index++) {
            EmployeePerformance item = employeeMetrics.get(index);
            ranked.add(new EmployeePerformance(item.id(), item.name(), item.sector(), item.tasks(), item.points(),
                    item.averagePoints(), item.participation(), item.evolution(), index + 1));
        }

        Map<UUID, List<Task>> bySector = tasks.stream().filter(item -> item.getBoard() != null && item.getBoard().getSector() != null)
                .collect(Collectors.groupingBy(item -> item.getBoard().getSector().getId()));
        Map<UUID, Long> members = employeeRepository.findAllByCityHallId_IdAndStatusTrueOrderByFirstNameAscLastNameAsc(cityId).stream()
                .filter(item -> item.getSectorId() != null).collect(Collectors.groupingBy(item -> item.getSectorId().getId(), Collectors.counting()));
        List<SectorPerformance> sectorMetrics = visibleSectors.stream().map(sector -> {
            List<Task> sectorTasks = bySector.getOrDefault(sector.getId(), List.of());
            long points = sectorTasks.stream().mapToLong(item -> item.getBusinessPoints()).sum();
            long memberCount = members.getOrDefault(sector.getId(), 0L);
            return new SectorPerformance(sector.getId(), sector.getName(), sectorTasks.size(), points, memberCount,
                    average(sectorTasks.size(), memberCount), average(points, memberCount), percentage(sectorTasks.size(), tasks.size()));
        }).sorted(Comparator.comparingLong(SectorPerformance::tasks).reversed()
                .thenComparing(Comparator.comparingLong(SectorPerformance::points).reversed())
                .thenComparing(SectorPerformance::name, String.CASE_INSENSITIVE_ORDER)).toList();

        long totalPoints = tasks.stream().mapToLong(item -> item.getBusinessPoints()).sum();
        String cityHallName = cityHallRepository.findById(cityId).map(item -> item.getName()).orElse("");
        return new ManagementResponse(cityHallName, errors.isEmpty(), errors,
                new Period(normalizedPeriod, PERIOD_LABELS.get(normalizedPeriod), start, end), selectedSectorId,
                !admin && visibleSectors.size() < sectorRepository.countByCityHall_Id(cityId),
                visibleSectors.stream().map(item -> new SectorOption(item.getId(), item.getName())).toList(),
                new Indicators(tasks.size(), totalPoints, average(tasks.size(), employees.size()),
                        average(totalPoints, employees.size()), average(totalPoints, tasks.size()),
                        byEmployee.size()), ranked, sectorMetrics, series,
                ranked.stream().filter(item -> item.tasks() > 0).map(item -> new EmployeeComparison(item.name(), item.sector(), item.tasks(), item.points())).toList(),
                tasks.stream().filter(item -> responsibles(item).isEmpty()).count());
    }

    private EmployeePerformance employeeMetric(Employee employee, List<Task> tasks, long total, LocalDate start, LocalDate end) {
        long points = tasks.stream().mapToLong(item -> item.getBusinessPoints()).sum();
        return new EmployeePerformance(employee.getId(), employee.getFullName(), employee.getSectorId() == null ? NO_SECTOR : employee.getSectorId().getName(),
                tasks.size(), points, average(points, tasks.size()), percentage(tasks.size(), total),
                temporalSeries(tasks, start, end).stream().map(TemporalPoint::value).toList(), 0);
    }

    private List<Employee> responsibles(Task task) {
        LinkedHashMap<UUID, Employee> result = new LinkedHashMap<>();
        if (task.getResponsible() != null) result.put(task.getResponsible().getId(), task.getResponsible());
        if (task.getResponsibles() != null) task.getResponsibles().forEach(item -> result.put(item.getId(), item));
        return new ArrayList<>(result.values());
    }

    private List<TemporalPoint> temporalSeries(List<Task> tasks, LocalDate start, LocalDate end) {
        long days = end.toEpochDay() - start.toEpochDay() + 1;
        List<LocalDate> points = new ArrayList<>();
        Function<LocalDate, LocalDate> key;
        DateTimeFormatter formatter;
        if (days <= 45) {
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) points.add(date);
            key = Function.identity(); formatter = DateTimeFormatter.ofPattern("dd/MM");
        } else if (days <= 186) {
            LocalDate first = start.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            for (LocalDate date = first; !date.isAfter(end); date = date.plusWeeks(1)) points.add(date);
            key = date -> date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)); formatter = DateTimeFormatter.ofPattern("dd/MM");
        } else {
            for (YearMonth month = YearMonth.from(start); !month.atDay(1).isAfter(end); month = month.plusMonths(1)) points.add(month.atDay(1));
            key = date -> date.withDayOfMonth(1); formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        }
        Map<LocalDate, Long> counts = tasks.stream().filter(item -> item.getCompletedAt() != null)
                .collect(Collectors.groupingBy(item -> key.apply(item.getCompletedAt().toLocalDate()), Collectors.counting()));
        return points.stream().map(point -> new TemporalPoint(point.format(formatter), counts.getOrDefault(point, 0L))).toList();
    }

    private double average(long total, long count) { return count == 0 ? 0 : Math.round(total * 100.0 / count) / 100.0; }
    private double percentage(long part, long total) { return total == 0 ? 0 : Math.round(part * 10000.0 / total) / 100.0; }

    private Employee require(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado");
        if (employee.getCityHallId() == null) throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        return employee;
    }
}
