package com.fiap.hackgov.tasks.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.tasks.internal.DTOs.Tasks.CreateTaskDTO;
import com.fiap.hackgov.tasks.internal.DTOs.Tasks.TaskResponseDTO;
import com.fiap.hackgov.tasks.internal.DTOs.Tasks.UpdateTaskDTO;
import com.fiap.hackgov.tasks.internal.entities.Board;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.mapper.TaskMapper;
import com.fiap.hackgov.tasks.internal.repositories.BoardRepository;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import com.fiap.hackgov.tasks.internal.repositories.TaskTimeEntryRepository;
import com.fiap.hackgov.inbox.internal.services.InboxService;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskReporitory taskRepository;
    private final TaskMapper taskMapper;
    private final EmployeeRepository employeeRepository;
    private final BoardRepository boardRepository;
    private final InboxService inboxService;
    private final TaskTimeEntryRepository timeEntryRepository;

    public TaskResponseDTO create(CreateTaskDTO dto, Employee authenticatedEmployee) {
        Employee currentEmployee = requireAuthenticated(authenticatedEmployee);
        UUID cityHallId = requireCityHallId(currentEmployee);

        Employee responsible = employeeRepository.findById(dto.responsible().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsavel pela tarefa nao encontrado"));

        Board board = boardRepository.findById(dto.board().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Quadro/setor de destino da tarefa nao encontrado"));

        validateSameCity(board, cityHallId);
        validateSameCity(responsible, cityHallId);
        validateResponsibleSector(responsible, board);

        Task task = taskMapper.toEntity(dto);

        task.setResponsible(responsible);
        task.setBoard(board);
        task.setCreatedBy(currentEmployee);
        task.setResponsibles(resolveResponsibles(dto.responsibleIds(), responsible, board, cityHallId));
        task.setStatus(dto.status() == null ? Task.Status.TODO : dto.status());
        task.setPriority(dto.priority() == null ? Task.Priority.NORMAL : dto.priority());
        task.setBusinessPoints(dto.businessPoints() == null ? 0 : dto.businessPoints());
        task.setProtocol(dto.protocol() == null ? "" : dto.protocol().trim());
        task.setExpectedResult(dto.expectedResult() == null ? "" : dto.expectedResult().trim());
        validateDates(task.getStartDate(), task.getEndDate());
        updateCompletion(task);

        Task saved = taskRepository.save(task);
        inboxService.notifyTask(saved, currentEmployee);
        return taskMapper.toDTO(saved);
    }

    public Page<TaskResponseDTO> findAll(Pageable pageable, Employee authenticatedEmployee) {
        Employee currentEmployee = requireAuthenticated(authenticatedEmployee);
        UUID cityHallId = requireCityHallId(currentEmployee);

        if (canViewCityTasks(currentEmployee)) {
            return taskRepository.findAllByBoard_CityHall_Id(cityHallId, pageable)
                    .map(taskMapper::toDTO);
        }

        return taskRepository.findAllByBoard_CityHall_IdAndBoard_Sector_Id(cityHallId, requireSectorId(currentEmployee), pageable)
                .map(taskMapper::toDTO);
    }

    public TaskResponseDTO findById(UUID id, Employee authenticatedEmployee) {
        return taskMapper.toDTO(findVisibleTask(id, requireAuthenticated(authenticatedEmployee)));
    }

    @Transactional
    public TaskResponseDTO update(UUID id, UpdateTaskDTO dto, Employee authenticatedEmployee) {
        Employee currentEmployee = requireAuthenticated(authenticatedEmployee);
        UUID cityHallId = requireCityHallId(currentEmployee);
        Task task = findVisibleTask(id, currentEmployee);
        requireTaskManager(task, currentEmployee);

        if (dto.title() != null && !dto.title().isBlank()) {
            task.setTitle(dto.title());
        }

        if (dto.description() != null && !dto.description().isBlank()) {
            task.setDescription(dto.description());
        }

        if (dto.responsibleId() != null) {
            Employee responsible = employeeRepository.findById(dto.responsibleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Responsavel pela tarefa nao encontrado"));
            validateSameCity(responsible, cityHallId);
            validateResponsibleSector(responsible, task.getBoard());
            task.setResponsible(responsible);
        }

        if (dto.boardId() != null) {
            Board board = boardRepository.findById(dto.boardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quadro/setor de destino da tarefa nao encontrado"));
            validateSameCity(board, cityHallId);
            task.setBoard(board);
        }

        if (dto.startDate() != null) {
            task.setStartDate(dto.startDate());
        }

        if (dto.endDate() != null) {
            task.setEndDate(dto.endDate());
        }

        if (dto.status() != null) task.setStatus(dto.status());
        if (dto.priority() != null) task.setPriority(dto.priority());
        if (dto.businessPoints() != null) task.setBusinessPoints(dto.businessPoints());
        if (dto.protocol() != null) task.setProtocol(dto.protocol().trim());
        if (dto.expectedResult() != null) task.setExpectedResult(dto.expectedResult().trim());
        if (dto.responsibleIds() != null) {
            task.setResponsibles(resolveResponsibles(dto.responsibleIds(), task.getResponsible(), task.getBoard(), cityHallId));
        }
        if (task.getResponsible() != null) {
            validateResponsibleSector(task.getResponsible(), task.getBoard());
            task.getResponsibles().add(task.getResponsible());
        }
        validateDates(task.getStartDate(), task.getEndDate());
        updateCompletion(task);
        if (Task.Status.COMPLETED.equals(task.getStatus())) stopActiveTimers(task);

        Task saved = taskRepository.save(task);
        inboxService.notifyTask(saved, currentEmployee);
        return taskMapper.toDTO(saved);
    }

    public void delete(UUID id, Employee authenticatedEmployee) {
        Employee current = requireAuthenticated(authenticatedEmployee);
        Task task = findVisibleTask(id, current);
        boolean soleResponsible = task.getResponsibles().size() == 1 && task.getResponsibles().stream()
                .anyMatch(employee -> employee.getId().equals(current.getId()));
        if (!Roles.ADMIN.equals(current.getRole()) && !soleResponsible) {
            throw new UnauthorizedException("Somente um administrador ou o unico responsavel pode excluir a tarefa");
        }
        taskRepository.delete(task);
    }

    private Set<Employee> resolveResponsibles(Set<UUID> ids, Employee primary, Board board, UUID cityHallId) {
        Set<Employee> result = new LinkedHashSet<>();
        if (primary != null) result.add(primary);
        if (ids != null) {
            for (UUID id : ids) {
                Employee employee = employeeRepository.findByIdWithDetails(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Responsavel pela tarefa nao encontrado: " + id));
                validateSameCity(employee, cityHallId);
                validateResponsibleSector(employee, board);
                result.add(employee);
            }
        }
        if (result.isEmpty()) throw new BusinessException("A tarefa precisa ter ao menos um responsavel");
        return result;
    }

    private void requireTaskManager(Task task, Employee employee) {
        if (Roles.ADMIN.equals(employee.getRole())) return;
        boolean responsible = task.getResponsible() != null && task.getResponsible().getId().equals(employee.getId())
                || task.getResponsibles().stream().anyMatch(item -> item.getId().equals(employee.getId()));
        if (!responsible) throw new UnauthorizedException("Somente responsaveis ou administradores podem alterar a tarefa");
    }

    private void validateResponsibleSector(Employee employee, Board board) {
        if (board != null && board.getSector() != null
                && (employee.getSectorId() == null || !board.getSector().getId().equals(employee.getSectorId().getId()))) {
            throw new BusinessException("O responsavel precisa pertencer ao setor de destino da tarefa");
        }
    }

    private void validateDates(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException("O prazo da tarefa nao pode ser anterior a data de inicio");
        }
    }

    private void updateCompletion(Task task) {
        if (Task.Status.COMPLETED.equals(task.getStatus())) {
            if (task.getCompletedAt() == null) task.setCompletedAt(java.time.LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }
    }

    private void stopActiveTimers(Task task) {
        java.time.LocalDateTime finishedAt = java.time.LocalDateTime.now();
        var activeEntries = timeEntryRepository.findByTask_IdAndManualFalseAndFinishedAtIsNull(task.getId());
        activeEntries.forEach(entry -> {
            entry.setFinishedAt(finishedAt);
            entry.setDurationSeconds(Math.max(0, java.time.Duration.between(entry.getStartedAt(), finishedAt).getSeconds()));
        });
        timeEntryRepository.saveAll(activeEntries);
    }

    private Task findVisibleTask(UUID id, Employee employee) {
        UUID cityHallId = requireCityHallId(employee);

        if (canViewCityTasks(employee)) {
            return taskRepository.findByIdAndBoard_CityHall_Id(id, cityHallId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarefa nao encontrada para esta prefeitura"));
        }

        return taskRepository.findByIdAndBoard_CityHall_IdAndBoard_Sector_Id(id, cityHallId, requireSectorId(employee))
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa nao encontrada para o seu setor"));
    }

    private Employee requireAuthenticated(Employee employee) {
        if (employee == null) {
            throw new UnauthorizedException("E necessario estar autenticado para acessar tarefas");
        }

        return employee;
    }

    private boolean canViewCityTasks(Employee employee) {
        return Roles.ADMIN.equals(employee.getRole());
    }

    private UUID requireCityHallId(Employee employee) {
        if (employee.getCityHallId() == null) {
            throw new BusinessException("O usuario autenticado precisa estar vinculado a uma prefeitura");
        }

        return employee.getCityHallId().getId();
    }

    private UUID requireSectorId(Employee employee) {
        if (employee.getSectorId() == null) {
            throw new BusinessException("O usuario autenticado precisa estar vinculado a um setor");
        }

        return employee.getSectorId().getId();
    }

    private void validateSameCity(Board board, UUID cityHallId) {
        if (board.getCityHall() == null || !cityHallId.equals(board.getCityHall().getId())) {
            throw new BusinessException("O quadro/setor de destino nao pertence a prefeitura do usuario autenticado");
        }
    }

    private void validateSameCity(Employee employee, UUID cityHallId) {
        if (employee.getCityHallId() == null || !cityHallId.equals(employee.getCityHallId().getId())) {
            throw new BusinessException("O responsavel informado nao pertence a prefeitura do usuario autenticado");
        }
    }
}
