package com.fiap.hackgov.bidding.internal.DTOs.Employee;

public record CreateUserRequestDTO(
        String firstName,
        String lastName,
        String email,
        String password,
        String role,
        String department,
        String phone
) {
}
