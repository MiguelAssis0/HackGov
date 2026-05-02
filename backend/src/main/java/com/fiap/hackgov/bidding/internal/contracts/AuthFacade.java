package com.fiap.hackgov.bidding.internal.contracts;

import com.fiap.hackgov.bidding.internal.DTOs.Employee.CreateUserRequestDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Employee.EmployeeDTO;

import java.util.UUID;

public interface AuthFacade {
    EmployeeDTO findById(UUID id);

    UUID createUser(CreateUserRequestDTO request);

    void deleteUser(UUID id);
}

