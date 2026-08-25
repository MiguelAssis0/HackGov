package com.fiap.hackgov.agenda.internal.services;

import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.Response;
import com.fiap.hackgov.agenda.internal.DTOs.AgendaEventDTOs.SaveRequest;
import com.fiap.hackgov.agenda.internal.entities.AgendaEvent;
import com.fiap.hackgov.agenda.internal.repositories.AgendaEventRepository;
import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgendaEventService {
    private final AgendaEventRepository eventRepository;
    private final TaskReporitory taskRepository;

    @Transactional(readOnly = true)
    public List<Response> findMonth(YearMonth month, UUID taskId, Employee employee) {
        Employee current = requireEmployee(employee);
        return eventRepository.findMonth(cityHallId(current), month.atDay(1), month.atEndOfMonth(), taskId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<Response> findUpcoming(int limit, Employee employee) {
        Employee current = requireEmployee(employee);
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return eventRepository.findUpcoming(cityHallId(current), LocalDate.now()).stream()
                .limit(safeLimit).map(this::toResponse).toList();
    }

    @Transactional
    public Response create(SaveRequest request, Employee employee) {
        Employee current = requireEmployee(employee);
        AgendaEvent event = new AgendaEvent();
        event.setCityHall(current.getCityHallId());
        event.setCreatedBy(current);
        apply(event, request, current);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public Response update(UUID id, SaveRequest request, Employee employee) {
        Employee current = requireEmployee(employee);
        AgendaEvent event = findScoped(id, current);
        requireOwnerOrAdmin(event, current);
        apply(event, request, current);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public void delete(UUID id, Employee employee) {
        Employee current = requireEmployee(employee);
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
        if (employee.getCityHallId() == null) throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        return employee.getCityHallId().getId();
    }

    private Response toResponse(AgendaEvent event) {
        Task task = event.getTask();
        return new Response(event.getId(), event.getTitle(), event.getDescription(), event.getType(),
                event.getStartDate(), event.getEndDate(), event.getStartTime(), event.getEndTime(),
                event.getLocation(), task == null ? null : task.getId(), task == null ? null : task.getTitle(),
                event.getCreatedAt(), event.getUpdatedAt());
    }
}
