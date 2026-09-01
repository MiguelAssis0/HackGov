package com.fiap.hackgov.audit.internal.services;

import com.fiap.hackgov.audit.internal.entities.AuditEvent;
import com.fiap.hackgov.audit.internal.repositories.AuditEventRepository;
import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.CityHallRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {
    @Mock
    AuditEventRepository repository;

    @Mock
    CityHallRepository cityHallRepository;

    @Test
    void appendCreatesHashLinkedToPreviousEvent() {
        AuditEvent previous = new AuditEvent();
        previous.setEventHash("a".repeat(64));
        Employee employee = employee();
        when(repository.findTopByCityHallIdOrderByIdDesc(employee.getCityHallId().getId())).thenReturn(Optional.of(previous));
        AuditEventService service = new AuditEventService(repository, cityHallRepository);

        service.append(employee, "POST", "/api/tasks", 201, "127.0.0.1", "JUnit");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        assertEquals(previous.getEventHash(), captor.getValue().getPreviousHash());
        assertEquals(64, captor.getValue().getEventHash().length());
        assertEquals(employee.getCityHallId().getId(), captor.getValue().getCityHallId());
    }

    private Employee employee() {
        CityHall cityHall = new CityHall();
        cityHall.setId(UUID.randomUUID());
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setEmail("admin@cidade.gov.br");
        employee.setCityHallId(cityHall);
        employee.setRole(Roles.ADMIN);
        return employee;
    }
}
