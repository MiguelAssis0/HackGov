package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.CreateUserRequestDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.contracts.AuthFacade;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.mapper.EmployeeMapper;
import com.fiap.hackgov.cityhall_management.internal.repositories.CityHallRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.utils.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private CityHallRepository cityHallRepository;

    @Autowired
    private AuthFacade authFacade;

    @Autowired
    private AuditLog auditLog;

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    @Transactional
    public Employee save(CreateEmployeeDTO employeeDTO) {

        auditLog.with(log)
                .event("save_employee")
                .email(employeeDTO.email())
                .level(AuditLog.Level.INFO)
                .log();

        CityHall cityHall = cityHallRepository.findById(employeeDTO.cityhallId())
                .orElseThrow(() -> {
                    auditLog.with(log)
                            .event("save_employee_failed")
                            .reason("city_hall_not_found: " + employeeDTO.cityhallId())
                            .level(AuditLog.Level.WARN)
                            .log();

                    return new ResourceNotFoundException("City Hall not found");
                });

        UUID userId = authFacade.createUser(
                new CreateUserRequestDTO(
                        employeeDTO.firstName(),
                        employeeDTO.lastName(),
                        employeeDTO.cpf(),
                        employeeDTO.email(),
                        employeeDTO.password(),
                        employeeDTO.phone()
                )
        );

        try {

            Employee employee = employeeMapper.toEntity(employeeDTO);

            employee.setCityhallId(cityHall);
            employee.setUserId(userId);

            Employee saved = employeeRepository.save(employee);

            auditLog.with(log)
                    .event("save_employee_success")
                    .level(AuditLog.Level.INFO)
                    .log();

            return saved;

        } catch (Exception e) {

            authFacade.deleteUser(userId);

            auditLog.with(log)
                    .event("save_employee_failed")
                    .reason("employee_persist_error")
                    .level(AuditLog.Level.ERROR)
                    .log();

            throw e;
        }
    }

    public Page<Employee> findAll(Pageable pageable) {
        auditLog.with(log).event("find_all_employees").level(AuditLog.Level.INFO).log();
        return employeeRepository.findAll(pageable);
    }

    public EmployeeDTO findById(UUID uuid) {
        auditLog.with(log).event("find_employee_by_id").level(AuditLog.Level.INFO).log();
        Employee employee = employeeRepository.findById(uuid)
                .orElseThrow(() -> {
                    auditLog.with(log).event("find_employee_by_id_failed").reason("employee_not_found").level(AuditLog.Level.WARN).log();
                    return new ResourceNotFoundException("Employee not found: " + uuid);
                });

        auditLog.with(log).event("find_employee_by_id_success").level(AuditLog.Level.INFO).log();
        return employeeMapper.toEmployeeDTO(employee);
    }
}
