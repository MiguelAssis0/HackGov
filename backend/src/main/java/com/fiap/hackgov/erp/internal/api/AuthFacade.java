package com.fiap.hackgov.erp.internal.api;

import com.fiap.hackgov.erp.internal.DTOs.Employee.CreateUserRequestDTO;
import com.fiap.hackgov.erp.internal.DTOs.Employee.EmployeeDTO;

import java.util.UUID;

public interface AuthFacade {
    EmployeeDTO findById(UUID id);

    UUID createUser(CreateUserRequestDTO request);

    void deleteUser(UUID id);
}

