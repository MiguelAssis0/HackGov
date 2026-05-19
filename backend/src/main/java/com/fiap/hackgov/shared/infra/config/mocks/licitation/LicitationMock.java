package com.fiap.hackgov.shared.infra.config.mocks.licitation;

import com.fiap.hackgov.bidding.internal.entities.LicitationHistory;
import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationType;
import com.fiap.hackgov.bidding.internal.repositories.LicitationHistoryRepository;
import com.fiap.hackgov.bidding.internal.repositories.LicitationProcessRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LicitationMock {

    private final LicitationProcessRepository licitationProcessRepository;
    private final LicitationHistoryRepository licitationHistoryRepository;

    public void load(MockContext ctx) {

        LicitationProcess licitationProcess = new LicitationProcess();

        licitationProcess.setProcessNumber("LIC-2026-000001");

        licitationProcess.setRequisition(ctx.requisitionInLicitation);

        licitationProcess.setType(LicitationType.PREGAO_ELETRONICO);

        licitationProcess.setStatus(LicitationStatus.IN_PROGRESS);

        licitationProcess.setEstimatedValue(new BigDecimal("150000.00"));

        licitationProcess.setObjectDescription("Contratação de empresa especializada em manutenção predial");

        licitationProcess.setOpeningDate(LocalDate.now().minusDays(5));

        licitationProcess.setClosingDate(LocalDate.now().plusDays(10));

        licitationProcess = licitationProcessRepository.save(licitationProcess);

        createHistory(licitationProcess, ctx.carlos, LicitationEventType.PROCESS_CREATED, LicitationStatus.DRAFT, "Processo licitatório criado");

        createHistory(licitationProcess, ctx.carlos, LicitationEventType.STATUS_CHANGED, LicitationStatus.OPEN, "Edital publicado");

        createHistory(licitationProcess, ctx.carlos, LicitationEventType.STATUS_CHANGED, LicitationStatus.IN_PROGRESS, "Recebimento de propostas iniciado");

        ctx.licitationProcess = licitationProcess;
    }

    private void createHistory(LicitationProcess licitationProcess, Employee employee, LicitationEventType eventType, LicitationStatus status, String observation) {

        LicitationHistory history = new LicitationHistory();

        history.setLicitationProcess(licitationProcess);

        history.setChangedBy(employee);

        history.setEventType(eventType);

        history.setStatus(status);

        history.setObservation(observation);

        history.setChangedAt(LocalDateTime.now());

        licitationHistoryRepository.save(history);
    }
}
