package com.fiap.hackgov.services;

import com.fiap.hackgov.entities.User;
import com.fiap.hackgov.repositories.ClientRepository;
import com.fiap.hackgov.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return employeeRepository.findUserByEmail(email)
                .map(u -> (User) u)
                .or(() -> clientRepository.findUserByEmail(email).map(u -> (User) u))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}