package com.fiap.hackgov.shared.infra.config.mocks.requisition;

import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.bidding.internal.entities.enums.*;
import com.fiap.hackgov.bidding.internal.repositories.*;
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
    private final AnalysisRepository analysisRepository;

    public void load(MockContext ctx) {

        /*
         * REQUISIÇÃO 1
         * Aguardando homologação do secretário
         */

        Requisition pendingApproval = new Requisition();

        pendingApproval.setRegisterNumber("REQ-2026-000001");

        pendingApproval.setSector(ctx.tiSectorsSP);

        pendingApproval.setResponsible(ctx.admin);

        pendingApproval.setTechnicalDescription("Aquisição de notebooks para equipe administrativa");

        pendingApproval.setJustification("Equipamentos atuais apresentam baixa performance");

        pendingApproval.setBudgetAllocation("4.4.90.52.00");
        pendingApproval.setType(AcquisitionType.BEM_MOVEL);

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

        createApproval(pendingApproval, ApprovalSector.REQUISICAO_SECRETARIO, ApprovalStatus.PENDENTE, null, null, null);

        /*
         * REQUISIÇÃO 2
         * Homologação do secretário aprovada, aguardando análise da requisição
         */

        Requisition pendingAnalysis = new Requisition();

        pendingAnalysis.setRegisterNumber("REQ-2026-000002");

        pendingAnalysis.setSector(ctx.comprasSectorSP);

        pendingAnalysis.setResponsible(ctx.ana);
        pendingAnalysis.setProcurementResponsible(ctx.ana);

        pendingAnalysis.setTechnicalDescription("Aquisição de insumos para atendimento municipal");

        pendingAnalysis.setJustification("Reposição de materiais essenciais para continuidade do atendimento");

        pendingAnalysis.setBudgetAllocation("3.3.90.30.00");
        pendingAnalysis.setType(AcquisitionType.BEM_MOVEL);

        pendingAnalysis = requisitionRepository.save(pendingAnalysis);

        ProcessStatus pendingAnalysisStatus = new ProcessStatus();

        pendingAnalysisStatus.setRequisition(pendingAnalysis);

        pendingAnalysisStatus.setStage(ProcessStage.ANALISE_REQUISICAO);

        pendingAnalysisStatus.setResponsibleId(ctx.ana.getId());

        pendingAnalysisStatus.setObservation("Aguardando análise da área de compras");

        processStatusRepository.save(pendingAnalysisStatus);

        pendingAnalysis.setProcessStatus(pendingAnalysisStatus);

        createHistory(pendingAnalysis, ctx.ana, ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.REQUISITION_CREATED, "Requisição criada");

        createHistory(pendingAnalysis, ctx.admin, ProcessStage.HOMOLOGACAO_SECRETARIO, HistoryEventType.APPROVED, "Homologação do secretário aprovada");

        createHistory(pendingAnalysis, ctx.ana, ProcessStage.RECEBIMENTO_COMPRAS, HistoryEventType.STAGE_SENT, "Requisição recebida pela área de compras");

        createHistory(pendingAnalysis, ctx.ana, ProcessStage.ANALISE_REQUISICAO, HistoryEventType.STAGE_SENT, "Requisição enviada para análise técnica");

        createApproval(pendingAnalysis, ApprovalSector.REQUISICAO_SECRETARIO, ApprovalStatus.APROVADO, ctx.admin, LocalDateTime.now().minusDays(1), "Abertura do processo homologada");

        createAnalysis(pendingAnalysis, ProcessStage.ANALISE_REQUISICAO, AnalysisResult.PENDENTE, null, null, "Aguardando validação documental e técnica");

        /*
         * REQUISIÇÃO 3
         * Análise aprovada, aguardando homologação da área de compras
         */

        Requisition pendingProcurementApproval = new Requisition();

        pendingProcurementApproval.setRegisterNumber("REQ-2026-000003");

        pendingProcurementApproval.setSector(ctx.comprasSectorSP);

        pendingProcurementApproval.setResponsible(ctx.maria);
        pendingProcurementApproval.setProcurementResponsible(ctx.ana);

        pendingProcurementApproval.setTechnicalDescription("Contratação de serviço de limpeza predial");

        pendingProcurementApproval.setJustification("Atendimento às unidades com maior circulação pública");

        pendingProcurementApproval.setBudgetAllocation("3.3.90.39.00");
        pendingProcurementApproval.setType(AcquisitionType.SERVICO_PJ);

        pendingProcurementApproval = requisitionRepository.save(pendingProcurementApproval);

        ProcessStatus pendingProcurementApprovalStatus = new ProcessStatus();

        pendingProcurementApprovalStatus.setRequisition(pendingProcurementApproval);

        pendingProcurementApprovalStatus.setStage(ProcessStage.HOMOLOGACAO_COMPRAS);

        pendingProcurementApprovalStatus.setResponsibleId(ctx.ana.getId());

        pendingProcurementApprovalStatus.setObservation("Aguardando homologação da área de compras");

        processStatusRepository.save(pendingProcurementApprovalStatus);

        pendingProcurementApproval.setProcessStatus(pendingProcurementApprovalStatus);

        createHistory(pendingProcurementApproval, ctx.maria, ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.REQUISITION_CREATED, "Requisição criada");

        createHistory(pendingProcurementApproval, ctx.admin, ProcessStage.HOMOLOGACAO_SECRETARIO, HistoryEventType.APPROVED, "Homologação do secretário aprovada");

        createHistory(pendingProcurementApproval, ctx.ana, ProcessStage.ANALISE_REQUISICAO, HistoryEventType.APPROVED, "Análise da requisição aprovada");

        createHistory(pendingProcurementApproval, ctx.maria, ProcessStage.HOMOLOGACAO_COMPRAS, HistoryEventType.STAGE_SENT, "Análise enviada para homologação da área de compras");

        createApproval(pendingProcurementApproval, ApprovalSector.REQUISICAO_SECRETARIO, ApprovalStatus.APROVADO, ctx.admin, LocalDateTime.now().minusDays(2), "Abertura do processo homologada");

        createAnalysis(pendingProcurementApproval, ProcessStage.ANALISE_REQUISICAO, AnalysisResult.APROVADO, ctx.ana, LocalDateTime.now().minusDays(1), "Documentação e ETP validados");

        createApproval(pendingProcurementApproval, ApprovalSector.ANALISE_COMPRAS, ApprovalStatus.PENDENTE, null, null, "Aguardando homologação da análise");

        /*
         * REQUISIÇÃO 4
         * Homologação e análise aprovadas, pronta para composição do processo
         */

        Requisition validatedFlow = new Requisition();

        validatedFlow.setRegisterNumber("REQ-2026-000004");

        validatedFlow.setSector(ctx.tiSectorsSP);

        validatedFlow.setResponsible(ctx.joao);
        validatedFlow.setProcurementResponsible(ctx.maria);

        validatedFlow.setTechnicalDescription("Renovação de certificados digitais institucionais");

        validatedFlow.setJustification("Manter serviços digitais assinados e juridicamente válidos");

        validatedFlow.setBudgetAllocation("3.3.90.40.00");
        validatedFlow.setType(AcquisitionType.SERVICO_PJ);

        validatedFlow = requisitionRepository.save(validatedFlow);

        ProcessStatus validatedFlowStatus = new ProcessStatus();

        validatedFlowStatus.setRequisition(validatedFlow);

        validatedFlowStatus.setStage(ProcessStage.COMPOSICAO_PROCESSO);

        validatedFlowStatus.setResponsibleId(ctx.maria.getId());

        validatedFlowStatus.setObservation("Fluxo de validação concluído e pronto para composição do processo");

        processStatusRepository.save(validatedFlowStatus);

        validatedFlow.setProcessStatus(validatedFlowStatus);

        createHistory(validatedFlow, ctx.joao, ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.REQUISITION_CREATED, "Requisição criada");

        createHistory(validatedFlow, ctx.admin, ProcessStage.HOMOLOGACAO_SECRETARIO, HistoryEventType.APPROVED, "Homologação do secretário aprovada");

        createHistory(validatedFlow, ctx.ana, ProcessStage.ANALISE_REQUISICAO, HistoryEventType.APPROVED, "Análise técnica aprovada");

        createHistory(validatedFlow, ctx.maria, ProcessStage.HOMOLOGACAO_COMPRAS, HistoryEventType.APPROVED, "Homologação da área de compras aprovada");

        createHistory(validatedFlow, ctx.maria, ProcessStage.COMPOSICAO_PROCESSO, HistoryEventType.STAGE_SENT, "Processo enviado para composição");

        createApproval(validatedFlow, ApprovalSector.REQUISICAO_SECRETARIO, ApprovalStatus.APROVADO, ctx.admin, LocalDateTime.now().minusDays(3), "Abertura do processo homologada");

        createAnalysis(validatedFlow, ProcessStage.ANALISE_REQUISICAO, AnalysisResult.APROVADO, ctx.ana, LocalDateTime.now().minusDays(2), "Requisição validada sem pendências");

        createApproval(validatedFlow, ApprovalSector.ANALISE_COMPRAS, ApprovalStatus.APROVADO, ctx.maria, LocalDateTime.now().minusDays(1), "Análise homologada pela área de compras");

        /*
         * REQUISIÇÃO 5
         * Já liberada para processo licitatório
         */

        Requisition licitationRequisition = new Requisition();

        licitationRequisition.setRegisterNumber("REQ-2026-000005");

        licitationRequisition.setSector(ctx.comprasSectorSP);

        licitationRequisition.setResponsible(ctx.maria);
        licitationRequisition.setProcurementResponsible(ctx.maria);

        licitationRequisition.setTechnicalDescription("Contratação de empresa para manutenção predial");

        licitationRequisition.setJustification("Necessidade de manutenção preventiva dos prédios públicos");

        licitationRequisition.setBudgetAllocation("3.3.90.39.00");
        licitationRequisition.setType(AcquisitionType.SERVICO_PJ);

        licitationRequisition = requisitionRepository.save(licitationRequisition);

        ProcessStatus licitationStatus = new ProcessStatus();

        licitationStatus.setRequisition(licitationRequisition);

        licitationStatus.setStage(ProcessStage.PROCESSO_LICITATORIO);

        licitationStatus.setResponsibleId(ctx.maria.getId());

        licitationStatus.setObservation("Processo em fase licitatória");

        processStatusRepository.save(licitationStatus);

        licitationRequisition.setProcessStatus(licitationStatus);

        createHistory(licitationRequisition, ctx.maria, ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.REQUISITION_CREATED, "Requisição criada");

        createHistory(licitationRequisition, ctx.maria, ProcessStage.HOMOLOGACAO_SECRETARIO, HistoryEventType.APPROVED, "Homologação do secretário aprovada");

        createHistory(licitationRequisition, ctx.ana, ProcessStage.RECEBIMENTO_COMPRAS, HistoryEventType.STAGE_SENT, "Processo enviado para área de compras");

        createHistory(licitationRequisition, ctx.maria, ProcessStage.PROCESSO_LICITATORIO, HistoryEventType.STAGE_SENT, "Processo encaminhado para licitação");

        createApproval(licitationRequisition, ApprovalSector.REQUISICAO_SECRETARIO, ApprovalStatus.APROVADO, ctx.admin, LocalDateTime.now().minusDays(4), "Abertura do processo homologada");

        createAnalysis(licitationRequisition, ProcessStage.ANALISE_REQUISICAO, AnalysisResult.APROVADO, ctx.ana, LocalDateTime.now().minusDays(3), "Requisição aprovada para composição do processo");

        createApproval(licitationRequisition, ApprovalSector.ANALISE_COMPRAS, ApprovalStatus.APROVADO, ctx.maria, LocalDateTime.now().minusDays(2), "Análise homologada pela área de compras");

        /*
         * REQUISIÇÃO 6
         * Processo licitatório concluído, pronta para contrato
         */

        Requisition completedLicitationRequisition = new Requisition();

        completedLicitationRequisition.setRegisterNumber("REQ-2026-000006");

        completedLicitationRequisition.setSector(ctx.financeiroSectorSP);

        completedLicitationRequisition.setResponsible(ctx.joao);
        completedLicitationRequisition.setProcurementResponsible(ctx.maria);

        completedLicitationRequisition.setTechnicalDescription("Contratação de licença e suporte para plataforma de gestão tributária");

        completedLicitationRequisition.setJustification("Modernização do atendimento e melhoria do controle fiscal");

        completedLicitationRequisition.setBudgetAllocation("3.3.90.40.00");
        completedLicitationRequisition.setType(AcquisitionType.SERVICO_PJ);

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

        createApproval(completedLicitationRequisition, ApprovalSector.REQUISICAO_SECRETARIO, ApprovalStatus.APROVADO, ctx.admin, LocalDateTime.now().minusDays(5), "Abertura do processo homologada");

        createAnalysis(completedLicitationRequisition, ProcessStage.ANALISE_REQUISICAO, AnalysisResult.APROVADO, ctx.ana, LocalDateTime.now().minusDays(4), "Análise da requisição aprovada");

        createApproval(completedLicitationRequisition, ApprovalSector.ANALISE_COMPRAS, ApprovalStatus.APROVADO, ctx.maria, LocalDateTime.now().minusDays(3), "Análise homologada pela área de compras");

        /*
         * CONTEXTO
         */

        ctx.requisitionPendingApproval = pendingApproval;

        ctx.requisitionPendingAnalysis = pendingAnalysis;

        ctx.requisitionPendingProcurementApproval = pendingProcurementApproval;

        ctx.requisitionWithValidatedFlow = validatedFlow;

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

    private void createApproval(Requisition requisition, ApprovalSector sector, ApprovalStatus status, Employee approvedBy, LocalDateTime approvedAt, String observation) {

        Approval approval = new Approval();

        approval.setRequisition(requisition);

        approval.setApprovalSector(sector);

        approval.setApprovalStatus(status);

        approval.setApprovedBy(approvedBy);

        approval.setApprovedAt(approvedAt);

        approval.setObservation(observation);

        approvalRepository.save(approval);
    }

    private void createAnalysis(Requisition requisition, ProcessStage stage, AnalysisResult result, Employee analyzedBy, LocalDateTime analyzedAt, String observation) {

        Analysis analysis = new Analysis();

        analysis.setRequisition(requisition);

        analysis.setStage(stage);

        analysis.setResult(result);

        analysis.setAnalyzedBy(analyzedBy);

        analysis.setAnalyzedAt(analyzedAt);

        analysis.setObservation(observation);

        analysisRepository.save(analysis);
    }
}
