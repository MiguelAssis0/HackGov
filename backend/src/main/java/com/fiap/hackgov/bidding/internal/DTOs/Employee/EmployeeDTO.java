package com.fiap.hackgov.bidding.internal.DTOs.Employee;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String role,
        String department,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
