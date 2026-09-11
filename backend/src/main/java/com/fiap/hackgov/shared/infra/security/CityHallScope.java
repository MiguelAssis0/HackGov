package com.fiap.hackgov.shared.infra.security;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.CityHallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Resolve a prefeitura vigente da requisição.
 *
 * <p>Regra (mesma convenção de platform admin da auditoria): somente
 * {@code admin@admin.com} pode atuar em outra prefeitura, informada pelo
 * header {@code X-City-Hall-Id} (validado contra o banco). Todos os demais
 * usam a prefeitura do próprio vínculo — comportamento inalterado.
 */
@Component
@RequiredArgsConstructor
public class CityHallScope {

    public static final String CITY_HALL_HEADER = "X-City-Hall-Id";
    private static final String PLATFORM_ADMIN_EMAIL = "admin@admin.com";

    private final CityHallRepository cityHallRepository;

    public UUID resolve(Employee employee) {
        UUID override = overrideFor(employee);
        if (override != null) return override;
        return employee.getCityHallId().getId();
    }

    public CityHall resolveEntity(Employee employee) {
        UUID override = overrideFor(employee);
        if (override != null) return cityHallRepository.findById(override).orElseThrow();
        return employee.getCityHallId();
    }

    private UUID overrideFor(Employee employee) {
        if (employee == null || !PLATFORM_ADMIN_EMAIL.equalsIgnoreCase(employee.getEmail())) return null;
        String raw = currentHeader();
        if (raw == null || raw.isBlank()) return null;
        UUID requested;
        try {
            requested = UUID.fromString(raw.trim());
        } catch (IllegalArgumentException invalid) {
            return null;
        }
        return cityHallRepository.findById(requested).map(CityHall::getId).orElse(null);
    }

    private String currentHeader() {
        try {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                return attributes.getRequest().getHeader(CITY_HALL_HEADER);
            }
        } catch (Exception ignored) {
            // fora de requisição web (jobs, testes): sem override
        }
        return null;
    }
}
