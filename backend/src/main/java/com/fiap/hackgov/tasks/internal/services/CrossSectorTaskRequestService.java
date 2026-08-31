package com.fiap.hackgov.tasks.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.CityHallRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.inbox.internal.services.InboxService;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tasks.internal.DTOs.CrossSectorRequestDTOs.Answer;
import com.fiap.hackgov.tasks.internal.DTOs.CrossSectorRequestDTOs.Create;
import com.fiap.hackgov.tasks.internal.DTOs.CrossSectorRequestDTOs.Response;
import com.fiap.hackgov.tasks.internal.entities.Board;
import com.fiap.hackgov.tasks.internal.entities.CrossSectorTaskRequest;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.BoardRepository;
import com.fiap.hackgov.tasks.internal.repositories.CrossSectorTaskRequestRepository;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrossSectorTaskRequestService {
    private final CrossSectorTaskRequestRepository repository;
    private final SectorRepository sectorRepository;
    private final CityHallRepository cityHallRepository;
    private final EmployeeRepository employeeRepository;
    private final BoardRepository boardRepository;
    private final TaskReporitory taskRepository;
    private final InboxService inboxService;

    @Transactional
    public Response create(Create dto, Employee employee) {
        Employee current = requireManaged(employee);
        Sector origin = sector(current);
        CityHall cityHall = cityHall(current);
        Sector destination = sectorRepository.findByIdAndCityHall_Id(dto.destinationSectorId(), cityHall.getId()).orElseThrow(() -> new BusinessException("Setor de destino invalido"));
        if (origin.getId().equals(destination.getId()))
            throw new BusinessException("A demanda precisa ser enviada para outro setor");
        CrossSectorTaskRequest item = new CrossSectorTaskRequest();
        item.setCityHall(cityHall);
        item.setOriginSector(origin);
        item.setDestinationSector(destination);
        item.setTitle(dto.title().trim());
        item.setDescription(dto.description() == null ? "" : dto.description().trim());
        item.setPriority(dto.priority() == null ? Task.Priority.NORMAL : dto.priority());
        item.setDeadline(dto.deadline());
        item.setRequestedBy(current);
        item = repository.save(item);
        inboxService.notifyCrossSectorRequest(item);
        return response(item);
    }

    @Transactional(readOnly = true)
    public List<Response> list(Employee employee) {
        Employee current = requireManaged(employee);
        UUID cityId = cityHall(current).getId();
        UUID sectorId = current.getSectorId() == null ? null : sector(current).getId();
        return repository.findByCityHall_IdOrderByCreatedAtDesc(cityId).stream().filter(item -> Roles.ADMIN.equals(current.getRole()) || (sectorId != null && (item.getOriginSector().getId().equals(sectorId) || item.getDestinationSector().getId().equals(sectorId)))).map(this::response).toList();
    }

    @Transactional
    public Response accept(UUID id, Answer answer, Employee employee) {
        Employee current = requireManaged(employee);
        CrossSectorTaskRequest item = pending(id, current);
        requireDestination(item, current);
        CityHall cityHall = cityHall(current);
        Board board = boardRepository.findFirstByCityHall_IdAndSector_Id(cityHall.getId(), item.getDestinationSector().getId()).orElseGet(() -> {
            Board value = new Board();
            value.setName(item.getDestinationSector().getName());
            value.setCityHall(cityHall);
            value.setSector(item.getDestinationSector());
            return boardRepository.save(value);
        });
        Task task = new Task();
        task.setTitle(item.getTitle());
        task.setDescription(item.getDescription());
        task.setPriority(item.getPriority());
        task.setStatus(Task.Status.IN_PROGRESS);
        task.setStartDate(LocalDateTime.now());
        task.setEndDate(item.getDeadline() == null ? null : item.getDeadline().atTime(23, 59));
        task.setBoard(board);
        task.setResponsible(current);
        task.setResponsibles(new LinkedHashSet<>(Set.of(current)));
        task.setCreatedBy(item.getRequestedBy());
        task = taskRepository.save(task);
        item.setGeneratedTask(task);
        finish(item, current, answer.feedback(), CrossSectorTaskRequest.Status.ACCEPTED);
        inboxService.completeObject(cityHall(current).getId(), "cross_sector_task_request", item.getId());
        inboxService.notifyRequestResult(item, "aceita");
        return response(item);
    }

    @Transactional
    public Response reject(UUID id, Answer answer, Employee employee) {
        Employee current = requireManaged(employee);
        CrossSectorTaskRequest item = pending(id, current);
        requireDestination(item, current);
        if (answer.feedback() == null || answer.feedback().isBlank())
            throw new BusinessException("Informe o motivo da recusa");
        finish(item, current, answer.feedback(), CrossSectorTaskRequest.Status.REJECTED);
        inboxService.completeObject(cityHall(current).getId(), "cross_sector_task_request", item.getId());
        inboxService.notifyRequestResult(item, "recusada");
        return response(item);
    }

    private void finish(CrossSectorTaskRequest item, Employee employee, String feedback, CrossSectorTaskRequest.Status status) {
        item.setStatus(status);
        item.setAnsweredBy(employee);
        item.setFeedback(feedback == null ? "" : feedback.trim());
        item.setAnsweredAt(LocalDateTime.now());
        repository.save(item);
    }

    private CrossSectorTaskRequest pending(UUID id, Employee employee) {
        CrossSectorTaskRequest item = repository.findByIdAndCityHall_Id(id, city(employee)).orElseThrow(() -> new ResourceNotFoundException("Demanda nao encontrada"));
        if (item.getStatus() != CrossSectorTaskRequest.Status.PENDING)
            throw new BusinessException("A demanda ja foi respondida");
        return item;
    }

    private void requireDestination(CrossSectorTaskRequest item, Employee employee) {
        if (!Roles.ADMIN.equals(employee.getRole()) && !item.getDestinationSector().getId().equals(sector(employee).getId()))
            throw new UnauthorizedException("Somente o setor de destino pode responder");
    }

    private Employee require(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado");
        city(employee);
        return employee;
    }

    // ponytail: AuthenticationPrincipal é detached (lazy proxies) — recarrega para evitar LazyInitializationException
    private Employee requireManaged(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado");
        if (employee.getCityHallId() == null) throw new BusinessException("Usuario sem prefeitura");
        return employeeRepository.findById(employee.getId()).orElseThrow(() -> new ResourceNotFoundException("Funcionario nao encontrado"));
    }

    private UUID city(Employee employee) {
        if (employee.getCityHallId() == null) throw new BusinessException("Usuario sem prefeitura");
        return employee.getCityHallId().getId();
    }

    private CityHall cityHall(Employee employee) {
        if (employee.getCityHallId() == null) throw new BusinessException("Usuario sem prefeitura");
        UUID id = employee.getCityHallId().getId();
        return cityHallRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Prefeitura nao encontrada"));
    }

    private Sector sector(Employee employee) {
        if (employee.getSectorId() == null) throw new BusinessException("Usuario sem setor");
        UUID id = employee.getSectorId().getId();
        return sectorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Setor nao encontrado"));
    }

    private Response response(CrossSectorTaskRequest item) {
        return new Response(item.getId(), item.getOriginSector().getId(), item.getOriginSector().getName(), item.getDestinationSector().getId(), item.getDestinationSector().getName(), item.getTitle(), item.getDescription(), item.getPriority(), item.getDeadline(), item.getStatus(), item.getRequestedBy().getFullName(), item.getAnsweredBy() == null ? null : item.getAnsweredBy().getFullName(), item.getFeedback(), item.getGeneratedTask() == null ? null : item.getGeneratedTask().getId(), item.getCreatedAt(), item.getAnsweredAt());
    }
}
