package com.fiap.hackgov.auth.internal.services;

import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.auth.internal.repositories.UserRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional // << garante que a sessão fica aberta
    public UserDetails loadUserByUsername(String username) {
        Employee employee = employeeRepository.findByEmailWithPermissions(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Força o carregamento enquanto a sessão está aberta
        employee.getOccupationId().getPermissions().size();

        return employee;
    }
}