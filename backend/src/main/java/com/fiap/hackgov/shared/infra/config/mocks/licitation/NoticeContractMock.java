package com.fiap.hackgov.shared.infra.config.mocks.licitation;

import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.entities.enums.*;
import com.fiap.hackgov.bidding.internal.repositories.*;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NoticeContractMock {

    private final SupplierRepository supplierRepository;
    private final NoticeRepository noticeRepository;
    private final ContractRepository contractRepository;
    private final LicitationProcessRepository licitationProcessRepository;
    private final LicitationHistoryRepository licitationHistoryRepository;

    public void load(MockContext ctx) {

        Supplier supplier = new Supplier();
        supplier.setCnpj("12.345.678/0001-90");
        supplier.setCorporateName("TechGov Solucoes Integradas Ltda");
        supplier.setTradeName("TechGov");
        supplier.setEmail("contato@techgov.com.br");
        supplier.setPhone("(11) 3333-4444");
        supplier.setLegalRepresentative("Fernanda Lima");
        supplier.setActive(true);
        supplier.setAddress(new Address(
                "Avenida Paulista",
                "1000",
                "10 andar",
                "Bela Vista",
                "Sao Paulo",
                "SP",
                "01310-100"
        ));
        supplier = supplierRepository.save(supplier);
        ctx.supplierWinner = supplier;

        Notice notice = new Notice();
        notice.setLicitationProcess(ctx.licitationProcess);
        notice.setNoticeNumber("PE-2026-000001");
        notice.setTitle("Edital de manutenção predial");
        notice.setObjectDescription("Contratação de empresa especializada em manutenção predial corretiva e preventiva");
        notice.setContent("Edital destinado à contratação de manutenção predial com atendimento sob demanda e visitas programadas.");
        notice.setStatus(NoticeStatus.PUBLISHED);
        notice.setPublicationDate(LocalDate.now().minusDays(5));
        notice.setProposalOpeningDate(LocalDate.now().minusDays(4));
        notice.setProposalClosingDate(LocalDate.now().plusDays(10));
        notice.setEstimatedValue(new BigDecimal("150000.00"));
        notice.setCreatedBy(ctx.maria);
        noticeRepository.save(notice);

        createHistory(ctx.licitationProcess, ctx.maria, LicitationEventType.NOTICE_PUBLISHED, ctx.licitationProcess.getStatus(), "Edital publicado: " + notice.getNoticeNumber());

        LicitationProcess finishedProcess = new LicitationProcess();
        finishedProcess.setProcessNumber("LIC-2026-000002");
        finishedProcess.setRequisition(ctx.requisitionFinishedLicitation);
        finishedProcess.setResponsible(ctx.maria);
        finishedProcess.setType(LicitationType.CONCORRENCIA);
        finishedProcess.setStatus(LicitationStatus.FINISHED);
        finishedProcess.setEstimatedValue(new BigDecimal("420000.00"));
        finishedProcess.setObjectDescription("Contratação de plataforma de gestão tributária com suporte técnico");
        finishedProcess.setOpeningDate(LocalDate.now().minusDays(40));
        finishedProcess.setClosingDate(LocalDate.now().minusDays(20));
        finishedProcess.setWinnerSupplier(supplier);
        finishedProcess = licitationProcessRepository.save(finishedProcess);

        createHistory(finishedProcess, ctx.maria, LicitationEventType.PROCESS_CREATED, LicitationStatus.DRAFT, "Processo licitatório criado");
        createHistory(finishedProcess, ctx.maria, LicitationEventType.NOTICE_PUBLISHED, LicitationStatus.OPEN, "Edital publicado e propostas abertas");
        createHistory(finishedProcess, ctx.maria, LicitationEventType.PROCESS_FINISHED, LicitationStatus.FINISHED, "Processo finalizado com fornecedor vencedor definido");

        Notice finishedNotice = new Notice();
        finishedNotice.setLicitationProcess(finishedProcess);
        finishedNotice.setNoticeNumber("CC-2026-000002");
        finishedNotice.setTitle("Edital para plataforma de gestão tributária");
        finishedNotice.setObjectDescription("Seleção de empresa para fornecimento de software de gestão tributária e suporte técnico especializado");
        finishedNotice.setContent("Edital de concorrência para contratação de solução tecnológica com implantação, treinamento e sustentação.");
        finishedNotice.setStatus(NoticeStatus.CLOSED);
        finishedNotice.setPublicationDate(LocalDate.now().minusDays(38));
        finishedNotice.setProposalOpeningDate(LocalDate.now().minusDays(36));
        finishedNotice.setProposalClosingDate(LocalDate.now().minusDays(21));
        finishedNotice.setEstimatedValue(new BigDecimal("420000.00"));
        finishedNotice.setCreatedBy(ctx.maria);
        noticeRepository.save(finishedNotice);

        Contract contract = new Contract();
        contract.setLicitationProcess(finishedProcess);
        contract.setSupplier(supplier);
        contract.setContractNumber("CTR-2026-000001");
        contract.setObjectDescription("Prestação de serviços de implantação, licenciamento e suporte da plataforma tributária");
        contract.setTotalValue(new BigDecimal("398500.00"));
        contract.setSignedAt(LocalDate.now().minusDays(10));
        contract.setStartDate(LocalDate.now().minusDays(7));
        contract.setEndDate(LocalDate.now().plusYears(1).minusDays(7));
        contract.setResponsible(ctx.maria);
        contract.setStatus(ContractStatus.ACTIVE);
        contractRepository.save(contract);

        createHistory(finishedProcess, ctx.maria, LicitationEventType.CONTRACT_CREATED, finishedProcess.getStatus(), "Contrato criado: " + contract.getContractNumber());

        ctx.finishedLicitationProcess = finishedProcess;
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
