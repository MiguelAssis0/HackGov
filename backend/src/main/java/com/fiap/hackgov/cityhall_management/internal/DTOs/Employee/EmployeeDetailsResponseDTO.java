package com.fiap.hackgov.cityhall_management.internal.DTOs.Employee;

import java.util.UUID;

public record EmployeeDetailsResponseDTO(
        UUID id,
        String name,
        String email,
        String cpf,
        String phone,
        String avatarPath,
        Boolean twoFactor,
        Boolean accessibility,
        String cityhall,
        String occupation,
        String sector
) {
}
