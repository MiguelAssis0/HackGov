package com.fiap.hackgov.bidding.internal.DTOs.Employee;

import jakarta.validation.constraints.*;

public record CreateUserRequestDTO(

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must have at most 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must have at most 50 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must have at most 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must have between 8 and 100 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
        String password,

        @NotBlank(message = "Role is required")
        @Size(max = 50, message = "Role must have at most 50 characters")
        String role,

        @NotBlank(message = "Department is required")
        @Size(max = 100, message = "Department must have at most 100 characters")
        String department,

        @Size(max = 20, message = "Phone must have at most 20 characters")
        String phone

) {
}
