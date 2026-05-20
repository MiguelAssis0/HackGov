package com.fiap.hackgov.cityhall_management.internal.DTOs.Employee;

public record EmployeeDetailsResponseDTO(
        String name,
        String cityhall,
        String occupation,
        String sector
) {
}
