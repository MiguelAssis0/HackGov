package com.fiap.hackgov.auth.internal.DTOs.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record CreateUserDTO(

        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]*$")
        String firstName,

        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]*$")
        String lastName,

        @NotBlank
        @CPF
        String cpf,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$"
        )
        String password,

        @NotBlank
        @Pattern(
                regexp = "^\\+55\\d{10,11}$"
        )
        String phone

) {}