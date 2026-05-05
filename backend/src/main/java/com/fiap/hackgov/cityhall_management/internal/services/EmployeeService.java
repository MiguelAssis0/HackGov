package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.CreationToken;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.mapper.EmployeeMapper;
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
    private CreationTokenService creationTokenService;

    @Autowired
    private CityHallService cityHallService;

    @Autowired
    private AuditLog auditLog;

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    @Transactional
    public Employee save(CreateEmployeeDTO employeeDTO) {

        CreationToken creationToken = creationTokenService.validateAndConsume(employeeDTO.tokenId());

        auditLog.with(log)
                .event("save_employee")
                .email(employeeDTO.email())
                .level(AuditLog.Level.INFO)
                .log();


        Employee employee = employeeMapper.toEntity(employeeDTO);

        employee.setCityHallId(creationToken.getCityHall());

        Employee saved = employeeRepository.save(employee);

        auditLog.with(log)
                .event("save_employee_success")
                .level(AuditLog.Level.INFO)
                .log();

        return saved;

    }

    public Page<Employee> findAll(Pageable pageable) {
        auditLog.with(log).event("find_all_employees").level(AuditLog.Level.INFO).log();
        return employeeRepository.findAll(pageable);
    }

    public Employee findById(UUID uuid) {
        auditLog.with(log).event("find_employee_by_id").level(AuditLog.Level.INFO).log();
        Employee employee = employeeRepository.findById(uuid)
                .orElseThrow(() -> {
                    auditLog.with(log).event("find_employee_by_id_failed").reason("employee_not_found").level(AuditLog.Level.WARN).log();
                    return new ResourceNotFoundException("Employee not found: " + uuid);
                });

        auditLog.with(log).event("find_employee_by_id_success").level(AuditLog.Level.INFO).log();
        return employee;
    }

    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    auditLog.with(log).event("find_employee_by_email_failed").reason("employee_not_found").level(AuditLog.Level.WARN).log();
                    return new ResourceNotFoundException("Employee not found: " + email);
                });
    }
}
