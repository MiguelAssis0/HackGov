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
public class ContractPaymentMock {

    private final RequisitionRepository requisitionRepository;
    private final ProcessStatusRepository processStatusRepository;
    private final ProcessHistoryRepository processHistoryRepository;
    private final ApprovalRepository approvalRepository;
    private final SupplierRepository supplierRepository;
    private final NoticeRepository noticeRepository;
    private final ContractRepository contractRepository;
    private final LicitationProcessRepository licitationProcessRepository;
    private final LicitationHistoryRepository licitationHistoryRepository;
    private final ExecutionOrderRepository executionOrderRepository;
    private final CommitmentRepository commitmentRepository;
    private final PaymentDeclarationRepository paymentDeclarationRepository;

    public void load(MockContext ctx) {

        Requisition requisition = new Requisition();
        requisition.setRegisterNumber("REQ-2026-000007");
        requisition.setSector(ctx.financeiroSectorSP);
        requisition.setResponsible(ctx.roberto);
        requisition.setTechnicalDescription("Contratação de serviços de conciliação bancária e relatórios financeiros");
        requisition.setJustification("Automatizar conferências financeiras e reduzir inconsistências em pagamentos municipais");
        requisition.setBudgetAllocation("3.3.90.39.00");
        requisition.setRequiresEtp(false);
        requisition.setRequestStatus(RequestStatus.APROVADA);
        requisition = requisitionRepository.save(requisition);

        ProcessStatus status = new ProcessStatus();
        status.setRequisition(requisition);
        status.setStage(ProcessStage.EXECUCAO_PAGAMENTO);
        status.setResponsibleId(ctx.roberto.getId());
        status.setObservation("Declaração aprovada e aguardando execução do pagamento");
        processStatusRepository.save(status);
        requisition.setProcessStatus(status);

        createProcessHistory(requisition, ctx.roberto, ProcessStage.REQUISICAO_CADASTRADA, HistoryEventType.REQUISITION_CREATED, "Requisição criada");
        createProcessHistory(requisition, ctx.admin, ProcessStage.HOMOLOGACAO_SECRETARIO, HistoryEventType.APPROVED, "Homologação do secretário aprovada");
        createProcessHistory(requisition, ctx.ana, ProcessStage.PROCESSO_LICITATORIO, HistoryEventType.STAGE_SENT, "Processo encaminhado para licitação");

        Supplier supplier = new Supplier();
        supplier.setCnpj("45.678.901/0001-23");
        supplier.setCorporateName("FinGov Tecnologia Financeira Ltda");
        supplier.setTradeName("FinGov");
        supplier.setEmail("contratos@fingov.com.br");
        supplier.setPhone("(11) 3444-7788");
        supplier.setLegalRepresentative("Marcos Azevedo");
        supplier.setActive(true);
        supplier.setAddress(new Address(
                "Rua Vergueiro",
                "2500",
                "Conjunto 1204",
                "Vila Mariana",
                "Sao Paulo",
                "SP",
                "04102-000"
        ));
        supplier = supplierRepository.save(supplier);

        LicitationProcess licitationProcess = new LicitationProcess();
        licitationProcess.setProcessNumber("LIC-2026-000003");
        licitationProcess.setRequisition(requisition);
        licitationProcess.setResponsible(ctx.maria);
        licitationProcess.setType(LicitationType.PREGAO_ELETRONICO);
        licitationProcess.setStatus(LicitationStatus.FINISHED);
        licitationProcess.setEstimatedValue(new BigDecimal("98000.00"));
        licitationProcess.setObjectDescription("Contratação de plataforma de conciliação bancária para a Secretaria da Fazenda de São Paulo");
        licitationProcess.setOpeningDate(LocalDate.now().minusDays(45));
        licitationProcess.setClosingDate(LocalDate.now().minusDays(30));
        licitationProcess.setWinnerSupplier(supplier);
        licitationProcess = licitationProcessRepository.save(licitationProcess);

        createLicitationHistory(licitationProcess, ctx.ana, LicitationEventType.PROCESS_CREATED, LicitationStatus.DRAFT, "Processo licitatório criado");
        createLicitationHistory(licitationProcess, ctx.ana, LicitationEventType.NOTICE_PUBLISHED, LicitationStatus.OPEN, "Edital publicado");
        createLicitationHistory(licitationProcess, ctx.maria, LicitationEventType.PROCESS_FINISHED, LicitationStatus.FINISHED, "Processo finalizado com fornecedor vencedor definido");

        Notice notice = new Notice();
        notice.setLicitationProcess(licitationProcess);
        notice.setNoticeNumber("PE-2026-000003");
        notice.setTitle("Edital para conciliação bancária");
        notice.setObjectDescription("Contratação de plataforma de conciliação bancária e relatórios financeiros");
        notice.setContent("Edital para contratação de solução SaaS com implantação, suporte e treinamento para a área financeira.");
        notice.setStatus(NoticeStatus.CLOSED);
        notice.setPublicationDate(LocalDate.now().minusDays(43));
        notice.setProposalOpeningDate(LocalDate.now().minusDays(41));
        notice.setProposalClosingDate(LocalDate.now().minusDays(31));
        notice.setEstimatedValue(new BigDecimal("98000.00"));
        notice.setCreatedBy(ctx.ana);
        noticeRepository.save(notice);

        Contract contract = new Contract();
        contract.setLicitationProcess(licitationProcess);
        contract.setSupplier(supplier);
        contract.setContractNumber("CTR-2026-000002");
        contract.setObjectDescription("Prestação de serviços de plataforma de conciliação bancária e suporte financeiro");
        contract.setTotalValue(new BigDecimal("92500.00"));
        contract.setSignedAt(LocalDate.now().minusDays(20));
        contract.setStartDate(LocalDate.now().minusDays(18));
        contract.setEndDate(LocalDate.now().plusYears(1).minusDays(18));
        contract.setResponsible(ctx.fernanda);
        contract.setStatus(ContractStatus.ACTIVE);
        contract = contractRepository.save(contract);

        createLicitationHistory(licitationProcess, ctx.fernanda, LicitationEventType.CONTRACT_CREATED, LicitationStatus.FINISHED, "Contrato criado: " + contract.getContractNumber());
        createProcessHistory(requisition, ctx.fernanda, ProcessStage.SETOR_CONTRATOS, HistoryEventType.STAGE_SENT, "Contrato formalizado pelo setor de contratos");

        ExecutionOrder executionOrder = new ExecutionOrder();
        executionOrder.setContract(contract);
        executionOrder.setType(ExecutionOrderType.SERVICE);
        executionOrder.setNumber("OS-2026-000001");
        executionOrder.setDescription("Ordem de serviço para implantação inicial da plataforma de conciliação bancária");
        executionOrder.setIssuedAt(LocalDate.now().minusDays(15));
        executionOrder.setIssuedBy(ctx.fernanda);
        executionOrder = executionOrderRepository.save(executionOrder);

        createProcessHistory(requisition, ctx.fernanda, ProcessStage.INICIO_SERVICOS, HistoryEventType.STAGE_SENT, "Ordem de serviço emitida: " + executionOrder.getNumber());

        Commitment commitment = new Commitment();
        commitment.setContract(contract);
        commitment.setExecutionOrder(executionOrder);
        commitment.setType(CommitmentType.ORDINARY);
        commitment.setCommitmentNumber("EMP-2026-000001");
        commitment.setReservedValue(new BigDecimal("92500.00"));
        commitment.setIssuedBy(ctx.roberto);
        commitment = commitmentRepository.save(commitment);

        createProcessHistory(requisition, ctx.roberto, ProcessStage.EMISSAO_EMPENHO, HistoryEventType.STAGE_SENT, "Empenho emitido: " + commitment.getCommitmentNumber());

        PaymentDeclaration declaration = new PaymentDeclaration();
        declaration.setCommitment(commitment);
        declaration.setType(PaymentDeclarationType.PAYMENT_AUTHORIZATION);
        declaration.setDescription("Declaração de recebimento da implantação inicial e autorização para pagamento da primeira parcela");
        declaration.setApprovedBy(ctx.admin);
        declaration.setSecretaryApproved(true);
        declaration = paymentDeclarationRepository.save(declaration);

        createPaymentApproval(requisition, ctx.admin);
        createProcessHistory(requisition, ctx.admin, ProcessStage.DECLARACAO_PAGAMENTO, HistoryEventType.APPROVED, "Declaração de pagamento aprovada pelo secretário");

        createProcessHistory(requisition, ctx.admin, ProcessStage.EXECUCAO_PAGAMENTO, HistoryEventType.STAGE_SENT, "Declaração aprovada e encaminhada para execução do pagamento");

        ctx.requisitionInPaymentStage = requisition;
        ctx.paymentStageSupplier = supplier;
        ctx.paymentStageLicitationProcess = licitationProcess;
        ctx.paymentStageContract = contract;
        ctx.paymentStageExecutionOrder = executionOrder;
        ctx.paymentStageCommitment = commitment;
        ctx.paymentStageDeclaration = declaration;
        ctx.paymentStagePayment = null;
    }

    private void createPaymentApproval(Requisition requisition, Employee employee) {
        Approval approval = new Approval();
        approval.setRequisition(requisition);
        approval.setApprovalSector(ApprovalSector.DECLARACAO_PAGAMENTO);
        approval.setApprovalStatus(ApprovalStatus.APROVADO);
        approval.setApprovedBy(employee);
        approval.setObservation("Declaração de pagamento autorizada no mock");
        approval.setApprovedAt(LocalDateTime.now().minusDays(4));
        approvalRepository.save(approval);
    }

    private void createProcessHistory(Requisition requisition, Employee employee, ProcessStage stage, HistoryEventType eventType, String observation) {
        ProcessHistory history = new ProcessHistory();
        history.setRequisition(requisition);
        history.setChangedBy(employee);
        history.setStage(stage);
        history.setEventType(eventType);
        history.setObservation(observation);
        history.setChangedAt(LocalDateTime.now());
        processHistoryRepository.save(history);
    }

    private void createLicitationHistory(LicitationProcess licitationProcess, Employee employee, LicitationEventType eventType, LicitationStatus status, String observation) {
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
