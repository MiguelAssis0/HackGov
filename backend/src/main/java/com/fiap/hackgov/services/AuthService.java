package com.fiap.hackgov.services;

import com.fiap.hackgov.DTOs.Auth.LoginRequestDTO;
import com.fiap.hackgov.DTOs.Auth.LoginResponseDTO;
import com.fiap.hackgov.DTOs.Auth.TwoFactorRequestDTO;
import com.fiap.hackgov.DTOs.Auth.TwoFactorResponseDTO;
import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.infra.exceptions.InvalidCredentialsException;
import com.fiap.hackgov.infra.security.TokenService;
import com.fiap.hackgov.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(loginRequest.email());
        
        if (employeeOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        Employee employee = employeeOpt.get();
        
        if (!employee.isStatus()) {
            throw new InvalidCredentialsException("Account is inactive");
        }
        
        if (!passwordEncoder.matches(loginRequest.password(), employee.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        employee.setLastLogin(LocalDateTime.now());
        employeeRepository.save(employee);
        
        if (employee.isTwoFactor()) {
            twoFactorAuthService.sendTwoFactorCode(employee.getEmail(), employee.getName());
            return new LoginResponseDTO(null, employee.getEmail(), employee.getName(), employee.getRole(), true);
        }
        
        String token = tokenService.generateToken(employee);
        return new LoginResponseDTO(token, employee.getEmail(), employee.getName(), employee.getRole(), false);
    }

    public TwoFactorResponseDTO verifyTwoFactor(TwoFactorRequestDTO twoFactorRequest) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(twoFactorRequest.email());
        
        if (employeeOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        Employee employee = employeeOpt.get();
        
        if (!employee.isTwoFactor()) {
            throw new InvalidCredentialsException("Two-factor authentication not enabled for this account");
        }
        
        boolean isValid = twoFactorAuthService.verifyCode(twoFactorRequest.email(), twoFactorRequest.code());
        
        if (!isValid) {
            throw new InvalidCredentialsException("Invalid two-factor code");
        }
        
        String token = tokenService.generateToken(employee);
        return new TwoFactorResponseDTO(token, "Two-factor authentication successful");
    }
}
