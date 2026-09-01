package com.fiap.hackgov.agenda.internal.services;

import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.Access;
import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.Response;
import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.SaveRequest;
import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.TaskDeadline;
import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.TaskOption;
import com.fiap.hackgov.agenda.internal.entities.AgendaEvent;
import com.fiap.hackgov.agenda.internal.repositories.AgendaEventRepository;
import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import com.fiap.hackgov.tools.internal.entities.ToolConfiguration;
import com.fiap.hackgov.tools.internal.repositories.ToolConfigurationRepository;
import com.fiap.hackgov.tools.internal.services.ToolPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgendaEventService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");
    private final AgendaEventRepository eventRepository;
    private final TaskReporitory taskRepository;
    private final SectorRepository sectorRepository;
    private final ToolConfigurationRepository toolRepository;
    private final ToolPermissionService permissionService;

    @Transactional(readOnly = true)
    public List<Response> findMonth(String month, UUID taskId, Employee employee) {
        Employee current = requireEmployee(employee);
        YearMonth selectedMonth = parseMonth(month);
        requireView(current);
        return eventRepository.findMonth(cityHallId(current), selectedMonth.atDay(1), selectedMonth.atEndOfMonth(), taskId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<Response> findUpcoming(int limit, Employee employee) {
        Employee current = requireEmployee(employee);
        requireView(current);
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return eventRepository.findUpcoming(cityHallId(current), LocalDate.now(BUSINESS_ZONE)).stream()
                .limit(safeLimit).map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskDeadline> findTasks(String month, UUID taskId, Employee employee) {
        Employee current = requireEmployee(employee);
        requireView(current);
        YearMonth selectedMonth = parseMonth(month);
        LocalDateTime start = selectedMonth.atDay(1).atStartOfDay();
        LocalDateTime end = selectedMonth.plusMonths(1).atDay(1).atStartOfDay();
        List<Task> tasks = Roles.ADMIN.equals(current.getRole())
                ? taskRepository.findAgendaTasks(cityHallId(current), Task.Status.COMPLETED, start, end)
                : current.getSectorId() == null ? List.of()
                : taskRepository.findAgendaTasksForSector(cityHallId(current), current.getSectorId().getId(), Task.Status.COMPLETED, start, end);
        return tasks.stream()
                .filter(task -> taskId == null || task.getId().equals(taskId))
                .map(this::toTaskDeadline)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskOption> findTaskOptions(Employee employee) {
        Employee current = requireEmployee(employee);
        requireView(current);
        List<Task> tasks = Roles.ADMIN.equals(current.getRole())
                ? taskRepository.findAgendaTaskOptions(cityHallId(current), Task.Status.COMPLETED)
                : current.getSectorId() == null ? List.of()
                : taskRepository.findAgendaTaskOptionsForSector(cityHallId(current), current.getSectorId().getId(), Task.Status.COMPLETED);
        return tasks.stream().map(this::toTaskOption).toList();
    }

    @Transactional(readOnly = true)
    public Access access(Employee employee) {
        Employee current = requireEmployee(employee);
        String sectorName = current.getSectorId() == null ? null
                : sectorRepository.findByIdAndCityHall_Id(current.getSectorId().getId(), cityHallId(current))
                .map(sector -> sector.getName()).orElse(null);
        return new Access(canView(current), canCreate(current), Roles.ADMIN.equals(current.getRole()),
                sectorName);
    }

    @Transactional
    public Response create(SaveRequest request, Employee employee) {
        Employee current = requireEmployee(employee);
        requireCreate(current);
        AgendaEvent event = new AgendaEvent();
        event.setCityHall(current.getCityHallId());
        event.setCreatedBy(current);
        apply(event, request, current);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public Response update(UUID id, SaveRequest request, Employee employee) {
        Employee current = requireEmployee(employee);
        requireCreate(current);
        AgendaEvent event = findScoped(id, current);
        requireOwnerOrAdmin(event, current);
        apply(event, request, current);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public void delete(UUID id, Employee employee) {
        Employee current = requireEmployee(employee);
        requireCreate(current);
        AgendaEvent event = findScoped(id, current);
        requireOwnerOrAdmin(event, current);
        eventRepository.delete(event);
    }

    private void apply(AgendaEvent event, SaveRequest request, Employee employee) {
        LocalDate end = request.endDate() == null ? request.startDate() : request.endDate();
        if (end.isBefore(request.startDate())) {
            throw new BusinessException("A data final nao pode ser anterior a data inicial");
        }
        if (end.equals(request.startDate()) && request.startTime() != null && request.endTime() != null
                && !request.endTime().isAfter(request.startTime())) {
            throw new BusinessException("O horario final precisa ser posterior ao horario inicial");
        }

        event.setTitle(request.title().trim());
        event.setDescription(request.description() == null ? "" : request.description().trim());
        event.setType(request.type());
        event.setStartDate(request.startDate());
        event.setEndDate(request.endDate());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setLocation(request.location() == null ? "" : request.location().trim());
        event.setTask(request.taskId() == null ? null : findVisibleTask(request.taskId(), employee));
    }

    private Task findVisibleTask(UUID id, Employee employee) {
        UUID cityId = cityHallId(employee);
        if (Roles.ADMIN.equals(employee.getRole())) {
            return taskRepository.findByIdAndBoard_CityHall_Id(id, cityId)
                    .orElseThrow(() -> new BusinessException("A tarefa precisa pertencer a prefeitura ativa"));
        }
        if (employee.getSectorId() == null) {
            throw new BusinessException("O funcionario precisa estar vinculado a um setor para associar tarefas");
        }
        return taskRepository.findByIdAndBoard_CityHall_IdAndBoard_Sector_Id(id, cityId, employee.getSectorId().getId())
                .orElseThrow(() -> new BusinessException("A tarefa precisa pertencer ao setor do funcionario"));
    }

    private YearMonth parseMonth(String value) {
        if (value != null && !value.isBlank()) {
            try {
                return YearMonth.parse(value);
            } catch (RuntimeException ignored) {
                // Django falls back to the current month for invalid values.
            }
        }
        return YearMonth.now(BUSINESS_ZONE);
    }

    private void requireView(Employee employee) {
        if (!canView(employee)) throw new UnauthorizedException("Sem permissao para acessar a agenda");
    }

    private void requireCreate(Employee employee) {
        if (!canCreate(employee)) throw new UnauthorizedException("Sem permissao para gerenciar a agenda");
    }

    private boolean canView(Employee employee) {
        ToolConfiguration tool = toolRepository.findByCityHall_IdAndSlug(cityHallId(employee), "agenda").orElse(null);
        if (tool != null && !tool.isMandatory() && !tool.isEnabled()) return false;
        return employee.getCityHallId() != null
                && (Roles.ADMIN.equals(employee.getRole())
                || tool == null && employee.getSectorId() != null
                || tool != null && (!tool.isRestricted() ? employee.getSectorId() != null : permissionService.canAccess("agenda", employee)));
    }

    private boolean canCreate(Employee employee) {
        if (!canView(employee)) return false;
        ToolConfiguration tool = toolRepository.findByCityHall_IdAndSlug(cityHallId(employee), "agenda").orElse(null);
        return Roles.ADMIN.equals(employee.getRole()) || tool == null || !tool.isRestricted()
                || permissionService.canManage("agenda", employee);
    }

    private AgendaEvent findScoped(UUID id, Employee employee) {
        return eventRepository.findByIdAndCityHall_Id(id, cityHallId(employee))
                .orElseThrow(() -> new ResourceNotFoundException("Evento nao encontrado para esta prefeitura"));
    }

    private void requireOwnerOrAdmin(AgendaEvent event, Employee employee) {
        if (!Roles.ADMIN.equals(employee.getRole())
                && (event.getCreatedBy() == null || !event.getCreatedBy().getId().equals(employee.getId()))) {
            throw new BusinessException("Somente o criador ou um administrador pode alterar este evento");
        }
    }

    private Employee requireEmployee(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado para acessar a agenda");
        cityHallId(employee);
        return employee;
    }

    private UUID cityHallId(Employee employee) {
        if (employee.getCityHallId() == null)
            throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        return employee.getCityHallId().getId();
    }

    private Response toResponse(AgendaEvent event) {
        Task task = event.getTask();
        var sector = task == null || task.getBoard() == null ? null : task.getBoard().getSector();
        return new Response(event.getId(), event.getTitle(), event.getDescription(), event.getType(),
                event.getStartDate(), event.getEndDate(), event.getStartTime(), event.getEndTime(),
                event.getLocation(), task == null ? null : task.getId(), task == null ? null : task.getTitle(),
                sector == null ? null : sector.getId(), sector == null ? null : sector.getName(),
                event.getCreatedAt(), event.getUpdatedAt());
    }

    private TaskDeadline toTaskDeadline(Task task) {
        var sector = task.getBoard().getSector();
        return new TaskDeadline(task.getId(), task.getTitle(), task.getEndDate().toLocalDate(),
                sector == null ? null : sector.getId(), sector == null ? null : sector.getName());
    }

    private TaskOption toTaskOption(Task task) {
        var sector = task.getBoard().getSector();
        return new TaskOption(task.getId(), task.getTitle(), sector == null ? null : sector.getId(),
                sector == null ? null : sector.getName());
    }
}
