package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.EmployeeDetailsResponseDTO;
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
import com.fiap.hackgov.shared.infra.filters.HibernateFilterActivator;
import com.fiap.hackgov.shared.infra.utils.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private HibernateFilterActivator hibernateFilterActivator;

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

    @Transactional(readOnly = true)
    public Page<Employee> findAll(Pageable pageable, Employee authenticatedEmployee) {
        auditLog.with(log).event("find_all_employees").level(AuditLog.Level.INFO).log();

        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), translateSort(pageable.getSort()));

        if (authenticatedEmployee.getCityHallId() == null || authenticatedEmployee.getCityHallId().getId() == null) {
            throw new BusinessException("Authenticated employee must be linked to a city hall");
        }

        if (!Roles.ADMIN.equals(authenticatedEmployee.getRole()) && (authenticatedEmployee.getSectorId() == null || authenticatedEmployee.getSectorId().getId() == null)) {
            throw new BusinessException("Authenticated employee must be linked to a sector");
        }

        hibernateFilterActivator.enableFilters(authenticatedEmployee);

        return employeeRepository.findAll(safePageable);
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
        Employee employee = employeeRepository.findByIdWithDetails(authenticatedEmployee.getId()).orElseThrow(() -> {
            auditLog.with(log).event("get_employee_details_failed").reason("employee_not_found").level(AuditLog.Level.WARN).log();
            return new ResourceNotFoundException("Employee not found: " + authenticatedEmployee.getId());
        });

        return employeeMapper.toEmployeeDetailsResponseDTO(employee);
    }

    @Transactional
    public Employee update(UUID id, com.fiap.hackgov.cityhall_management.internal.DTOs.Employee.UpdateEmployeeDTO dto, Employee actor) {
        Employee target = employeeRepository.findByIdWithDetails(id).orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        if (!target.getCityHallId().getId().equals(actor.getCityHallId().getId()))
            throw new BusinessException("Funcionario deve pertencer a mesma prefeitura");
        // ponytail: Django can_manage = is_city_admin or platform_admin or has_perm; aqui ADMIN pode editar
        if (!com.fiap.hackgov.auth.internal.entities.enums.Roles.ADMIN.equals(actor.getRole()))
            throw new BusinessException("Sem permissao para editar funcionarios");
        if (dto.firstName() != null && !dto.firstName().isBlank()) target.setFirstName(dto.firstName().trim());
        if (dto.lastName() != null) {
            String ln = dto.lastName().trim();
            if (!ln.isBlank()) target.setLastName(ln);
        }
        if (dto.email() != null && !dto.email().isBlank()) target.setEmail(dto.email().trim().toLowerCase());
        if (dto.cpf() != null && !dto.cpf().isBlank()) target.setCpf(dto.cpf().replaceAll("\\D", ""));
        if (dto.phone() != null && !dto.phone().isBlank()) target.setPhone(dto.phone().replaceAll("\\D", ""));
        if (dto.registrationNumber() != null && !dto.registrationNumber().isBlank()) target.setRegistrationNumber(dto.registrationNumber().trim());
        if (dto.sectorId() != null) {
            Sector s = sectorRepository.findByIdAndCityHall_Id(dto.sectorId(), target.getCityHallId().getId()).orElseThrow(() -> new ResourceNotFoundException("Sector not found"));
            target.setSectorId(s);
        }
        if (dto.occupationId() != null) {
            Occupation o = occupationRepository.findById(dto.occupationId()).orElseThrow(() -> new ResourceNotFoundException("Occupation not found"));
            target.setOccupationId(o);
        }
        if (dto.salary() != null) target.setSalary(dto.salary());
        if (dto.hoursWorked() != null) target.setHoursWorked(dto.hoursWorked());
        if (dto.admissionDate() != null) target.setAdmissionDate(dto.admissionDate());
        if (dto.dismissalDate() != null) target.setDismissalDate(dto.dismissalDate());
        if (dto.status() != null) target.setStatus(dto.status());
        if (dto.isAdminCidade() != null) target.setRole(dto.isAdminCidade() ? com.fiap.hackgov.auth.internal.entities.enums.Roles.ADMIN : com.fiap.hackgov.auth.internal.entities.enums.Roles.EMPLOYEE);
        return employeeRepository.save(target);
    }

    @Transactional
    public Employee toggle(UUID id, Employee actor) {
        Employee target = employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        if (!target.getCityHallId().getId().equals(actor.getCityHallId().getId()))
            throw new BusinessException("Funcionario deve pertencer a mesma prefeitura");
        if (!com.fiap.hackgov.auth.internal.entities.enums.Roles.ADMIN.equals(actor.getRole()))
            throw new BusinessException("Sem permissao para alterar status");
        target.setStatus(!Boolean.TRUE.equals(target.getStatus()));
        return employeeRepository.save(target);
    }

    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email).orElseThrow(() -> {
            auditLog.with(log).event("find_employee_by_email_failed").reason("employee_not_found").level(AuditLog.Level.WARN).log();
            return new ResourceNotFoundException("Employee not found: " + email);
        });
    }

    private Sort translateSort(Sort sort) {

        List<Sort.Order> orders = new ArrayList<>();

        for (Sort.Order order : sort) {

            String property = switch (order.getProperty()) {
                case "name" -> "firstName";
                default -> order.getProperty();
            };

            orders.add(new Sort.Order(order.getDirection(), property));
        }

        return Sort.by(orders);
    }
}
