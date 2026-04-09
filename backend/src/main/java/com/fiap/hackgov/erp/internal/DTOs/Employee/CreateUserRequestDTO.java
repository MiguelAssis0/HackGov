package com.fiap.hackgov.erp.internal.DTOs.Employee;

public record CreateUserRequestDTO(
        String firstName,
        String lastName,
        String cpf,
        String email,
        String password,
        String phone
) {}
