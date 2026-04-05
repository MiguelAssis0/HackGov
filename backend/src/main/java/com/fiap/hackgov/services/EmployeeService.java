package com.fiap.hackgov.services;

import com.fiap.hackgov.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.entities.CityHall;
import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.infra.exceptions.EmployeeAlreadyExistsException;
import com.fiap.hackgov.infra.exceptions.EmployeeNotFoundException;
import com.fiap.hackgov.infra.security.TokenService;
import com.fiap.hackgov.infra.utils.AuditLog;
import com.fiap.hackgov.mapper.EmployeeMapper;
import com.fiap.hackgov.repositories.CityHallRepository;
import com.fiap.hackgov.repositories.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private TokenService tokenService;

    @Autowired
    private CityHallRepository cityHallRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLog auditLog;

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    @Transactional
    public Employee save(CreateEmployeeDTO employeeDTO) {
        auditLog.with(log).event("save_employee").email(employeeDTO.email()).level(AuditLog.Level.INFO).log();

        if(employeeRepository.findByEmail(employeeDTO.email()).isPresent()){
            auditLog.with(log).event("save_employee_failed").reason("email_already_exists").email(employeeDTO.email()).level(AuditLog.Level.WARN).log();
            throw new EmployeeAlreadyExistsException("Email already exists");
        }

        CityHall cityHall = cityHallRepository.findById(employeeDTO.cityHallId())
                .orElseThrow(() -> {
                    auditLog.with(log).event("save_employee_failed").reason("city_hall_not_found").level(AuditLog.Level.WARN).log();
                    return new IllegalArgumentException("City Hall not found");
                });

        Employee employee = employeeMapper.toEntity(employeeDTO);
        employee.setPassword(passwordEncoder.encode(employeeDTO.password()));
        employee.setCityhall(cityHall);

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
