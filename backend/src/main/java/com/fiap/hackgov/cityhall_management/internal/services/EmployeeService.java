package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.EmployeeDetailsResponseDTO;
import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.mapper.EmployeeMapper;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.OccupationRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.utils.AuditLog;
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
    private CityHallService cityHallService;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private OccupationRepository occupationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLog auditLog;

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    @Transactional
    public Employee save(CreateEmployeeDTO employeeDTO) {
        CityHall cityHall = cityHallService.findById(employeeDTO.cityHallId());
        Sector sector = sectorRepository.findByIdAndCityHall_Id(employeeDTO.sectorId(), cityHall.getId()).orElseThrow(() -> new ResourceNotFoundException("Sector not found for city hall: " + employeeDTO.sectorId()));
        Occupation occupation = occupationRepository.findById(employeeDTO.occupationId()).orElseThrow(() -> new ResourceNotFoundException("Occupation not found: " + employeeDTO.occupationId()));

        if (occupation.getSectorId() == null || !occupation.getSectorId().getId().equals(sector.getId())) {
            throw new BusinessException("Occupation does not belong to the informed sector");
        }

        auditLog.with(log).event("save_employee").email(employeeDTO.email()).level(AuditLog.Level.INFO).log();

        Employee employee = employeeMapper.toEntity(employeeDTO);
        employee.setPassword(passwordEncoder.encode(employeeDTO.password()));
        employee.setRole(Roles.EMPLOYEE);
        employee.setStatus(true);
        employee.setTwoFactor(false);
        employee.setCityHallId(cityHall);
        employee.setSectorId(sector);
        employee.setOccupationId(occupation);

        Employee saved = employeeRepository.save(employee);

        auditLog.with(log).event("save_employee_success").level(AuditLog.Level.INFO).log();

        return saved;

    }

    public Page<Employee> findAll(Pageable pageable) {
        auditLog.with(log).event("find_all_employees").level(AuditLog.Level.INFO).log();
        return employeeRepository.findAll(pageable);
    }

    public Employee findById(UUID uuid) {
        auditLog.with(log).event("find_employee_by_id").level(AuditLog.Level.INFO).log();
        Employee employee = employeeRepository.findById(uuid).orElseThrow(() -> {
            auditLog.with(log).event("find_employee_by_id_failed").reason("employee_not_found").level(AuditLog.Level.WARN).log();
            return new ResourceNotFoundException("Employee not found: " + uuid);
        });

        auditLog.with(log).event("find_employee_by_id_success").level(AuditLog.Level.INFO).log();
        return employee;
    }

    @Transactional(readOnly = true)
    public EmployeeDetailsResponseDTO getEmployeeDetails(Employee authenticatedEmployee) {
        Employee employee = employeeRepository.findByIdWithDetails(authenticatedEmployee.getId())
                .orElseThrow(() -> {
                    auditLog.with(log).event("get_employee_details_failed").reason("employee_not_found").level(AuditLog.Level.WARN).log();
                    return new ResourceNotFoundException("Employee not found: " + authenticatedEmployee.getId());
                });

        CityHall cityHall = employee.getCityHallId();
        Occupation occupation = employee.getOccupationId();
        Sector sector = employee.getSectorId();

        return new EmployeeDetailsResponseDTO(
                employee.getFullName(),
                cityHall != null ? cityHall.getName() : null,
                occupation != null ? occupation.getName() : null,
                sector != null ? sector.getName() : null
        );
    }

    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email).orElseThrow(() -> {
            auditLog.with(log).event("find_employee_by_email_failed").reason("employee_not_found").level(AuditLog.Level.WARN).log();
            return new ResourceNotFoundException("Employee not found: " + email);
        });
    }
}
