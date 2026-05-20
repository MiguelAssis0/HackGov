package com.fiap.hackgov.shared.infra.filters;


import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.security.SecurityContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HibernateFilterActivator {

    private final EntityManager entityManager;
    private final SecurityContext securityContext;

    public void enableFilters() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Employee)) {
            return;
        }

        Session session = entityManager.unwrap(Session.class);
        Employee employee = (Employee) auth.getPrincipal();

        session.enableFilter("cityHallFilter")
                .setParameter("cityHallId", employee.getCityHallId().getId());

        if (employee.getSectorId() != null && !Roles.ADMIN.equals(employee.getRole())) {
            session.enableFilter("sectorFilter")
                    .setParameter("sectorId", employee.getSectorId().getId());
        }
    }

    public void disableFilters() {
        try {
            Session session = entityManager.unwrap(Session.class);
            session.disableFilter("cityHallFilter");
            session.disableFilter("sectorFilter");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

