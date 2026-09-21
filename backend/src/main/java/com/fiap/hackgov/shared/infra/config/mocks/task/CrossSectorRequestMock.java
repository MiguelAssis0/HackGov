package com.fiap.hackgov.shared.infra.config.mocks.task;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.inbox.internal.services.InboxService;
import com.fiap.hackgov.tasks.internal.entities.CrossSectorTaskRequest;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.CrossSectorTaskRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CrossSectorRequestMock {
    private final CrossSectorTaskRequestRepository repository;
    private final EmployeeRepository employeeRepository;
    private final SectorRepository sectorRepository;
    private final InboxService inboxService;

    private static final String TITLE = "Elaborar memorando para evento do Dia dos Professores";
    private static final String DESCRIPTION = "Elaborar um memorando destinado ao Setor de Cultura, solicitando a organização de um evento em parceria com o Setor de Educação para comemorar o Dia dos Professores.\n\nO documento deverá formalizar a proposta de realização da celebração, incluindo a definição das atividades, programação, local, data e demais providências necessárias para a organização do evento em homenagem aos profissionais da educação.";

    // ponytail: roda também em banco já populado — idempotente por título pendente
    public void load(CityHall cityHall) {
        boolean exists = repository.findByCityHall_IdOrderByCreatedAtDesc(cityHall.getId()).stream()
                .anyMatch(item -> item.getStatus() == CrossSectorTaskRequest.Status.PENDING && TITLE.equals(item.getTitle()));
        if (exists) return;
        Employee camila = employeeRepository.findByEmail("camila.cultura@sp.gov.br").orElse(null);
        Sector cultura = sectorRepository.findByNameAndCityHall_Id("Cultura", cityHall.getId()).orElse(null);
        Sector compras = sectorRepository.findByNameAndCityHall_Id("Compras", cityHall.getId()).orElse(null);
        if (camila == null || cultura == null || compras == null) return;
        CrossSectorTaskRequest item = new CrossSectorTaskRequest();
        item.setCityHall(cityHall);
        item.setOriginSector(cultura);
        item.setDestinationSector(compras);
        item.setTitle(TITLE);
        item.setDescription(DESCRIPTION);
        item.setPriority(Task.Priority.NORMAL);
        item.setDeadline(LocalDate.now().plusDays(20));
        item.setRequestedBy(camila);
        repository.save(item);
        inboxService.notifyCrossSectorRequest(item);
    }
}
