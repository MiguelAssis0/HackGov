package com.fiap.hackgov.shared.infra.security;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SecurityContext {

    public Employee getAuthenticatedEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();


        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        if (!(auth.getPrincipal() instanceof Employee)) {
            throw new AccessDeniedException("Invalid principal type: " +
                    auth.getPrincipal().getClass().getName());
        }

        return (Employee) auth.getPrincipal();
    }

    public UUID getCurrentCityHallId() {
        return getAuthenticatedEmployee().getCityHallId().getId();
    }

    public UUID getCurrentSectorId() {
        return getAuthenticatedEmployee().getSectorId().getId();
    }

    public Set<String> getCurrentPermissions() {
        Employee employee = getAuthenticatedEmployee();
        return employee.getOccupationId()
                .getPermissions()
                .stream()
                .flatMap(p -> p.getPk().getPermission().getAction()
                        .stream()
                        .map(action -> p.getPk().getPermission().getResource() + ":" + action.name())
                )
                .collect(Collectors.toSet());
    }
}
