package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.filters.HibernateFilterActivator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class BiddingTenantIsolationIntegrationTest {
    @Autowired RequisitionRepository requisitionRepository;
    @Autowired EmployeeRepository employeeRepository;
    @Autowired SectorRepository sectorRepository;
    @Autowired HibernateFilterActivator filterActivator;
    @Autowired EntityManager entityManager;

    @Test
    void hidesRequisitionsFromAnotherCityHall() {
        Employee admin = employeeRepository.findByEmail("admin@admin.com").orElseThrow();
        var ownSector = sectorRepository.findAll().stream()
                .filter(item -> item.getCityHall().getId().equals(admin.getCityHallId().getId())).findFirst().orElseThrow();
        var otherSector = sectorRepository.findAll().stream()
                .filter(item -> !item.getCityHall().getId().equals(admin.getCityHallId().getId())).findFirst().orElseThrow();

        filterActivator.disableFilters();
        Requisition own = new Requisition();
        own.setRegisterNumber("TEST-OWN-" + UUID.randomUUID());
        own.setSector(ownSector);
        own = requisitionRepository.saveAndFlush(own);
        Requisition foreign = new Requisition();
        foreign.setRegisterNumber("TEST-FOREIGN-" + UUID.randomUUID());
        foreign.setSector(otherSector);
        foreign = requisitionRepository.saveAndFlush(foreign);

        entityManager.clear();
        filterActivator.enableFilters(admin);
        assertThat(requisitionRepository.findById(own.getId())).isPresent();
        assertThat(requisitionRepository.findById(foreign.getId())).isEmpty();
    }
}
