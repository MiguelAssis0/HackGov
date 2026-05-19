package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.CreationToken;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.CreationTokenRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreationTokenService {

    @Autowired
    private CreationTokenRepository creationTokenRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public CreationToken generateToken(String email) {

        Employee admin = employeeRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + email));

        CreationToken creationToken = new CreationToken();
        creationToken.setCityHall(admin.getCityHallId());
        creationToken.setUsed(false);
        return creationTokenRepository.save(creationToken);

    }

    public CreationToken validateAndConsume(UUID token) {
        CreationToken creationToken = creationTokenRepository
                .findByIdAndUsedFalse(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido ou já utilizado"));

        creationToken.setUsed(true);
        return creationTokenRepository.save(creationToken);
    }

}
