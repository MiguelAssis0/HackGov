package com.fiap.hackgov.shared.infra.config.mocks.requisition;

import com.fiap.hackgov.bidding.internal.entities.Approval;
import com.fiap.hackgov.bidding.internal.entities.ProcessHistory;
import com.fiap.hackgov.bidding.internal.entities.ProcessStatus;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalSector;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.HistoryEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.repositories.ApprovalRepository;
import com.fiap.hackgov.bidding.internal.repositories.ProcessHistoryRepository;
import com.fiap.hackgov.bidding.internal.repositories.ProcessStatusRepository;
import com.fiap.hackgov.bidding.internal.repositories.RequisitionRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RequisitionMock {

    private final RequisitionRepository requisitionRepository;
    private final ProcessStatusRepository processStatusRepository;
    private final ProcessHistoryRepository processHistoryRepository;
    private final ApprovalRepository approvalRepository;

    public void load(MockContext ctx) {

        /*
         * REQUISIÇÃO 1
         * Aguardando homologação do secretário
         */

        Requisition pendingApproval = new Requisition();

        pendingApproval.setRegisterNumber("REQ-2026-000001");

        pendingApproval.setSector(ctx.tiSector);

        pendingApproval.setResponsible(ctx.admin);

        pendingApproval.setTechnicalDescription("Aquisição de notebooks para equipe administrativa");

        pendingApproval.setJustification("Equipamentos atuais apresentam baixa performance");

        pendingApproval.setBudgetAllocation("4.4.90.52.00");

        pendingApproval = requisitionRepository.save(pendingApproval);

        ProcessStatus pendingStatus = new ProcessStatus();

        pendingStatus.setRequisition(pendingApproval);

        pendingStatus.setStage(ProcessStage.HOMOLOGACAO_SECRETARIO);

        pendingStatus.setResponsibleId(ctx.admin.getId());

        pendingStatus.setObservation("Aguardando homologação do secretário");

        processStatusRepository.save(pendingStatus);

        pendingApproval.setProcessStatus(pendingStatus);

        createHistory(pendingApproval, ctx.admin, ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.REQUISITION_CREATED, "Requisição criada");

        createHistory(pendingApproval, ctx.admin, ProcessStage.HOMOLOGACAO_SECRETARIO, HistoryEventType.STAGE_SENT, "Requisição enviada para homologação do secretário");

        Approval approvalSecretary = new Approval();

        approvalSecretary.setRequisition(pendingApproval);

        approvalSecretary.setApprovalSector(ApprovalSector.REQUISICAO_SECRETARIO);

        approvalSecretary.setApprovalStatus(ApprovalStatus.PENDENTE);

        approvalRepository.save(approvalSecretary);

        /*
         * REQUISIÇÃO 2
         * Já liberada para processo licitatório
         */

        Requisition licitationRequisition = new Requisition();

        licitationRequisition.setRegisterNumber("REQ-2026-000002");

        licitationRequisition.setSector(ctx.comprasSector);

        licitationRequisition.setResponsible(ctx.maria);

        licitationRequisition.setTechnicalDescription("Contratação de empresa para manutenção predial");

        licitationRequisition.setJustification("Necessidade de manutenção preventiva dos prédios públicos");

        licitationRequisition.setBudgetAllocation("3.3.90.39.00");

        licitationRequisition = requisitionRepository.save(licitationRequisition);

        ProcessStatus licitationStatus = new ProcessStatus();

        licitationStatus.setRequisition(licitationRequisition);

        licitationStatus.setStage(ProcessStage.PROCESSO_LICITATORIO);

        licitationStatus.setResponsibleId(ctx.carlos.getId());

        licitationStatus.setObservation("Processo em fase licitatória");

        processStatusRepository.save(licitationStatus);

        licitationRequisition.setProcessStatus(licitationStatus);

        createHistory(licitationRequisition, ctx.maria, ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.REQUISITION_CREATED, "Requisição criada");

        createHistory(licitationRequisition, ctx.maria, ProcessStage.HOMOLOGACAO_SECRETARIO, HistoryEventType.APPROVED, "Homologação do secretário aprovada");

        createHistory(licitationRequisition, ctx.carlos, ProcessStage.RECEBIMENTO_COMPRAS, HistoryEventType.STAGE_SENT, "Processo enviado para área de compras");

        createHistory(licitationRequisition, ctx.carlos, ProcessStage.PROCESSO_LICITATORIO, HistoryEventType.STAGE_SENT, "Processo encaminhado para licitação");

        Approval approvalCompleted = new Approval();

        approvalCompleted.setRequisition(licitationRequisition);

        approvalCompleted.setApprovalSector(ApprovalSector.REQUISICAO_SECRETARIO);

        approvalCompleted.setApprovalStatus(ApprovalStatus.APROVADO);

        approvalCompleted.setApprovedBy(ctx.admin);

        approvalCompleted.setApprovedAt(LocalDateTime.now().minusDays(2));

        approvalRepository.save(approvalCompleted);

        /*
         * REQUISIÇÃO 3
         * Processo licitatório concluído, pronta para contrato
         */

        Requisition completedLicitationRequisition = new Requisition();

        completedLicitationRequisition.setRegisterNumber("REQ-2026-000003");

        completedLicitationRequisition.setSector(ctx.financeiroSector);

        completedLicitationRequisition.setResponsible(ctx.joao);

        completedLicitationRequisition.setTechnicalDescription("Contratação de licença e suporte para plataforma de gestão tributária");

        completedLicitationRequisition.setJustification("Modernização do atendimento e melhoria do controle fiscal");

        completedLicitationRequisition.setBudgetAllocation("3.3.90.40.00");

        completedLicitationRequisition = requisitionRepository.save(completedLicitationRequisition);

        ProcessStatus completedLicitationStatus = new ProcessStatus();

        completedLicitationStatus.setRequisition(completedLicitationRequisition);

        completedLicitationStatus.setStage(ProcessStage.INICIO_SERVICOS);

        completedLicitationStatus.setResponsibleId(ctx.maria.getId());

        completedLicitationStatus.setObservation("Processo licitatório finalizado e aguardando execução contratual");

        processStatusRepository.save(completedLicitationStatus);

        completedLicitationRequisition.setProcessStatus(completedLicitationStatus);

        createHistory(completedLicitationRequisition, ctx.joao, ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.REQUISITION_CREATED, "Requisição criada");

        createHistory(completedLicitationRequisition, ctx.admin, ProcessStage.HOMOLOGACAO_SECRETARIO, HistoryEventType.APPROVED, "Homologação do secretário aprovada");

        createHistory(completedLicitationRequisition, ctx.maria, ProcessStage.PROCESSO_LICITATORIO, HistoryEventType.STAGE_SENT, "Processo encaminhado para licitação");

        createHistory(completedLicitationRequisition, ctx.maria, ProcessStage.INICIO_SERVICOS, HistoryEventType.STAGE_SENT, "Processo contratual iniciado após licitação concluída");

        /*
         * CONTEXTO
         */

        ctx.requisitionPendingApproval = pendingApproval;

        ctx.requisitionInLicitation = licitationRequisition;

        ctx.requisitionFinishedLicitation = completedLicitationRequisition;
    }

    private void createHistory(Requisition requisition, Employee employee, ProcessStage stage, HistoryEventType eventType, String observation) {

        ProcessHistory history = new ProcessHistory();

        history.setRequisition(requisition);

        history.setChangedBy(employee);

        history.setStage(stage);

        history.setEventType(eventType);

        history.setObservation(observation);

        history.setChangedAt(LocalDateTime.now());

        processHistoryRepository.save(history);
    }
}
