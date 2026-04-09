package com.fiap.hackgov.erp.internal.services;

import com.fiap.hackgov.erp.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.erp.internal.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.erp.internal.entities.CityHall;
import com.fiap.hackgov.erp.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.exceptions.EmployeeAlreadyExistsException;
import com.fiap.hackgov.shared.infra.exceptions.EmployeeNotFoundException;
import com.fiap.hackgov.shared.infra.utils.AuditLog;
import com.fiap.hackgov.erp.internal.mapper.EmployeeMapper;
import com.fiap.hackgov.erp.internal.repositories.CityHallRepository;
import com.fiap.hackgov.erp.internal.repositories.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLog auditLog;

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    @Transactional
    public Employee save(CreateEmployeeDTO employeeDTO) {
        auditLog.with(log).event("save_employee").email(employeeDTO.userId().toString()).level(AuditLog.Level.INFO).log();

        CityHall cityHall = cityHallRepository.findById(employeeDTO.cityhallId())
                .orElseThrow(() -> {
                    auditLog.with(log).event("save_employee_failed").reason("city_hall_not_found").level(AuditLog.Level.WARN).log();
                    return new IllegalArgumentException("City Hall not found");
                });

        Employee employee = employeeMapper.toEntity(employeeDTO);
        employee.setCityhallId(cityHall);

        auditLog.with(log).event("save_employee_success").level(AuditLog.Level.INFO).log();

        return employeeRepository.save(employee);
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
                    return new EmployeeNotFoundException("Employee not found");
                });

        auditLog.with(log).event("find_employee_by_id_success").level(AuditLog.Level.INFO).log();
        return employeeMapper.toEmployeeDTO(employee);
    }
}
