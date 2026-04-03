package com.fiap.hackgov.services;

import com.fiap.hackgov.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.entities.CityHall;
import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.infra.exceptions.EmployeeAlreadyExistsException;
import com.fiap.hackgov.infra.exceptions.EmployeeNotFoundException;
import com.fiap.hackgov.infra.security.TokenService;
import com.fiap.hackgov.mapper.EmployeeMapper;
import com.fiap.hackgov.repositories.CityHallRepository;
import com.fiap.hackgov.repositories.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
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

    @Transactional
    public Employee save(CreateEmployeeDTO employeeDTO) {
        if(employeeRepository.findByName(employeeDTO.name()).isPresent())
            throw new EmployeeAlreadyExistsException("Employee already exists");

        if(employeeRepository.findByEmail(employeeDTO.email()).isPresent())
            throw new EmployeeAlreadyExistsException("Email already exists");

        if(employeeDTO.role() == null)
            throw new IllegalArgumentException("Role is required");

        CityHall cityHall = cityHallRepository.findById(employeeDTO.cityHallId())
                .orElseThrow(() -> new IllegalArgumentException("City Hall not found"));

        Employee employee = employeeMapper.toEntity(employeeDTO);
        employee.setPassword(passwordEncoder.encode(employeeDTO.password()));
        employee.setCityhall(cityHall);

        return employeeRepository.save(employee);
    }

    public Page<Employee> findAll(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    public EmployeeDTO findById(String id, HttpServletRequest token) {
        try {
            UUID uuid = UUID.fromString(id);
            Employee employee = employeeRepository.findById(uuid)
                    .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

            return employeeMapper.toEmployeeDTO(employee);

        } catch (IllegalArgumentException e) {
            throw new EmployeeNotFoundException("Invalid employee ID");
        }
    }
}
