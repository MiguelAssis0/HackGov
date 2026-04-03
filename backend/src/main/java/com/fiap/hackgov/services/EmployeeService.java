package com.fiap.hackgov.services;

import com.fiap.hackgov.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.entities.CityHall;
import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.infra.exceptions.EmployeeAlreadyExistsException;
import com.fiap.hackgov.infra.exceptions.EmployeeNotFoundException;
import com.fiap.hackgov.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.infra.security.TokenService;
import com.fiap.hackgov.mapper.EmployeeMapper;
import com.fiap.hackgov.repositories.CityHallRepository;
import com.fiap.hackgov.repositories.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        employee.setPassword(new Argon2PasswordEncoder(10, 1024, 1, 1024, 1).encode(employeeDTO.password()));
        employee.setCityhall(cityHall);

        return employeeRepository.save(employee);
    }

    public Page<Employee> findAll(HttpServletRequest token, Pageable pageable) {
        tokenService.validateToken(token);
        return employeeRepository.findAll(pageable);
    }

    public EmployeeDTO findById(String id, HttpServletRequest token) {
        try {
            var isTokenValid = tokenService.validateToken(token);
            if(isTokenValid == null)
                throw new UnauthorizedException("User not authorized");
            UUID uuid = UUID.fromString(id);
            Employee employee = employeeRepository.findById(uuid)
                    .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

            return employeeMapper.toEmployeeDTO(employee);

        } catch (IllegalArgumentException e) {
            throw new EmployeeNotFoundException("Invalid employee ID");
        }
    }
}
