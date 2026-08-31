package com.fiap.hackgov.clients.internal.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class ClientDTOs {
    private ClientDTOs() {
    }

    public record SaveRequest(@NotBlank @Size(max = 180) String fullName, @Size(max = 100) String nickname,
                              @NotBlank @Pattern(regexp = "\\d{11}|[.\\-\\d]{14}") String cpf,
                              @NotBlank @Size(max = 30) String phone,
                              @Size(max = 100) String secondaryContact, @Size(max = 500) String address,
                              @Size(max = 50) String stateRegistration, @Size(max = 50) String caf) {
    }

    public record ServiceRequest(@NotBlank @Size(max = 140) String area, @NotBlank @Size(max = 280) String description,
                                 @Size(max = 2000) String observation, @PastOrPresent LocalDate serviceDate) {
    }

    public record ServiceResponse(UUID id, String area, String description, String observation, LocalDate serviceDate,
                                  String createdByName, LocalDateTime createdAt) {
    }

    public record Response(UUID id, String fullName, String nickname, String cpf, String phone, String secondaryContact,
                           String address,
                           String stateRegistration, String caf, boolean masked, List<ServiceResponse> services,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
    }
}
