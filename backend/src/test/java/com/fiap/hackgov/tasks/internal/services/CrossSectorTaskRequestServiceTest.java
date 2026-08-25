package com.fiap.hackgov.tasks.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.*;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.tasks.internal.DTOs.CrossSectorRequestDTOs.Create;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrossSectorTaskRequestServiceTest {
    @Mock CrossSectorTaskRequestRepository repository; @Mock SectorRepository sectorRepository;
    @Mock BoardRepository boardRepository; @Mock TaskReporitory taskRepository;

    @Test
    void createRejectsDestinationEqualToOriginSector() {
        CityHall city = new CityHall(); city.setId(UUID.randomUUID());
        Sector sector = new Sector(); sector.setId(UUID.randomUUID()); sector.setCityHall(city);
        Employee employee = new Employee(); employee.setId(UUID.randomUUID()); employee.setCityHallId(city); employee.setSectorId(sector);
        when(sectorRepository.findByIdAndCityHall_Id(sector.getId(), city.getId())).thenReturn(Optional.of(sector));
        CrossSectorTaskRequestService service = new CrossSectorTaskRequestService(repository, sectorRepository, boardRepository, taskRepository, null);

        assertThrows(BusinessException.class, () -> service.create(
                new Create(sector.getId(), "Demanda", "Descricao", Task.Priority.NORMAL, null), employee));
        verify(repository, never()).save(any());
    }
}
