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
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskReporitory taskRepository;
    private final TaskMapper taskMapper;
    private final EmployeeRepository employeeRepository;
    private final BoardRepository boardRepository;

    public TaskResponseDTO create(CreateTaskDTO dto, Employee authenticatedEmployee) {
        Employee currentEmployee = requireAuthenticated(authenticatedEmployee);
        UUID cityHallId = requireCityHallId(currentEmployee);

        Employee responsible = employeeRepository.findById(dto.responsible().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Board board = boardRepository.findById(dto.board().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        validateSameCity(board, cityHallId);
        validateSameCity(responsible, cityHallId);

        Task task = taskMapper.toEntity(dto);

        task.setResponsible(responsible);
        task.setBoard(board);
        task.setCreatedBy(currentEmployee);

        return taskMapper.toDTO(taskRepository.save(task));
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

        if (dto.title() != null && !dto.title().isBlank()) {
            task.setTitle(dto.title());
        }

        if (dto.description() != null && !dto.description().isBlank()) {
            task.setDescription(dto.description());
        }

        if (dto.responsibleId() != null) {
            Employee responsible = employeeRepository.findById(dto.responsibleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
            validateSameCity(responsible, cityHallId);
            task.setResponsible(responsible);
        }

        if (dto.boardId() != null) {
            Board board = boardRepository.findById(dto.boardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
            validateSameCity(board, cityHallId);
            task.setBoard(board);
        }

        if (dto.startDate() != null) {
            task.setStartDate(dto.startDate());
        }

        if (dto.endDate() != null) {
            task.setEndDate(dto.endDate());
        }

        return taskMapper.toDTO(taskRepository.save(task));
    }

    public void delete(UUID id, Employee authenticatedEmployee) {
        Task task = findVisibleTask(id, requireAuthenticated(authenticatedEmployee));
        taskRepository.delete(task);
    }

    private Task findVisibleTask(UUID id, Employee employee) {
        UUID cityHallId = requireCityHallId(employee);

        if (canViewCityTasks(employee)) {
            return taskRepository.findByIdAndBoard_CityHall_Id(id, cityHallId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        }

        return taskRepository.findByIdAndBoard_CityHall_IdAndBoard_Sector_Id(id, cityHallId, requireSectorId(employee))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private Employee requireAuthenticated(Employee employee) {
        if (employee == null) {
            throw new UnauthorizedException("Authenticated employee is required");
        }

        return employee;
    }

    private boolean canViewCityTasks(Employee employee) {
        return Roles.ADMIN.equals(employee.getRole());
    }

    private UUID requireCityHallId(Employee employee) {
        if (employee.getCityHallId() == null) {
            throw new BusinessException("Employee must be linked to a city hall");
        }

        return employee.getCityHallId().getId();
    }

    private UUID requireSectorId(Employee employee) {
        if (employee.getSectorId() == null) {
            throw new BusinessException("Employee must be linked to a sector");
        }

        return employee.getSectorId().getId();
    }

    private void validateSameCity(Board board, UUID cityHallId) {
        if (board.getCityHall() == null || !cityHallId.equals(board.getCityHall().getId())) {
            throw new BusinessException("Board does not belong to the authenticated employee city hall");
        }
    }

    private void validateSameCity(Employee employee, UUID cityHallId) {
        if (employee.getCityHallId() == null || !cityHallId.equals(employee.getCityHallId().getId())) {
            throw new BusinessException("Responsible employee does not belong to the authenticated employee city hall");
        }
    }
}
