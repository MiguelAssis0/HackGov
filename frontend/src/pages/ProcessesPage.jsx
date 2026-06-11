import { useDeferredValue, useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { api } from "../services/api.js";
import { biddingApi } from "../services/biddingApi.js";

const PAGE_SIZE = 10;

const workflow = [
  { section: "Secretaria de Origem", code: "REQUISICAO_CADASTRADA", label: "Cadastro da Requisição" },
  { code: "HOMOLOGACAO_SECRETARIO", label: "Homologação Secretário" },
  { section: "Setor de Compras", code: "RECEBIMENTO_COMPRAS", label: "Recebimento Compras" },
  { code: "ANALISE_REQUISICAO", label: "Análise da Requisição" },
  { code: "HOMOLOGACAO_COMPRAS", label: "Homologação Compras" },
  { code: "COMPOSICAO_PROCESSO", label: "Composição do Processo" },
  { code: "PROCESSO_LICITATORIO", label: "Processo Licitatório" },
  { code: "SETOR_CONTRATOS", label: "Setor de Contratos" },
  { section: "Secretaria de Origem", code: "INICIO_SERVICOS", label: "Início dos Serviços" },
  { code: "EMISSAO_EMPENHO", label: "Emissão de Empenho" },
  { code: "DECLARACAO_PAGAMENTO", label: "Declaração p/ Pagamento" },
  { section: "Secretaria da Fazenda", code: "EXECUCAO_PAGAMENTO", label: "Execução do Pagamento" },
  { section: "Controle / Prestação", code: "PRESTACAO_CONTAS", label: "Enc. Prestação de Contas" },
  { code: "ANALISE_PRESTACAO_CONTAS", label: "Análise Prestação Contas" },
  { code: "HOMOLOGACAO_PRESTACAO_CONTAS", label: "Homologação Prestação" },
];

const emptyForm = {
  sectorId: "",
  type: "",
  technicalDescription: "",
  justification: "",
  budgetAllocation: "",
  etpContent: "",
};

const acquisitionTypes = [
  { value: "BEM_MOVEL", label: "Bem Móvel" },
  { value: "BEM_IMOVEL", label: "Bem Imóvel" },
  { value: "SERVICO_PJ", label: "Serviço (PJ)" },
  { value: "EMENDA_IMPOSITIVA", label: "Emenda Impositiva" },
];

const analysisStages = new Set(["ANALISE_REQUISICAO", "ANALISE_PRESTACAO_CONTAS"]);
const approvalStages = new Set([
  "HOMOLOGACAO_SECRETARIO",
  "HOMOLOGACAO_COMPRAS",
  "DECLARACAO_PAGAMENTO",
  "HOMOLOGACAO_PRESTACAO_CONTAS",
]);

const analysisActions = [
  { value: "APROVADO", label: "Aprovar", icon: "bi-check-circle", className: "process-action-success" },
  { value: "CORRECAO_NECESSARIA", label: "Solicitar correção", icon: "bi-arrow-counterclockwise", className: "process-action-warning" },
  { value: "REPROVADO", label: "Reprovar", icon: "bi-x-circle", className: "process-action-danger" },
  { value: "CANCELADO", label: "Cancelar", icon: "bi-slash-circle", className: "process-action-neutral" },
];

const approvalActions = [
  { value: "APROVADO", label: "Aprovar", icon: "bi-check-circle", className: "process-action-success" },
  { value: "REPROVADO", label: "Reprovar", icon: "bi-x-circle", className: "process-action-danger" },
];

const licitationTypes = [
  { value: "CHAMADA_PUBLICA", label: "Chamada Pública" },
  { value: "CONCORRENCIA_PUBLICA", label: "Concorrência Pública" },
  { value: "CONVITES_NP", label: "Convites (N/P)" },
  { value: "EXTRATO_FOMENTO", label: "Extrato de Fomento" },
  { value: "PREGAO_ELETRONICO", label: "Pregão Eletrônico" },
  { value: "PREGAO_PRESENCIAL", label: "Pregão Presencial" },
  { value: "TOMADA_PRECOS", label: "Tomada de Preços" },
  { value: "OUTROS", label: "Outros" },
];

const licitationResultActions = [
  { value: "IN_PROGRESS", label: "Andamento", icon: "bi-play-circle", className: "process-action-success" },
  { value: "FINISHED", label: "Finalizado", icon: "bi-trophy", className: "process-action-success" },
  { value: "IMPUGNED", label: "Impugnado", icon: "bi-exclamation-octagon", className: "process-action-danger" },
  { value: "POSTPONED", label: "Postergado", icon: "bi-calendar2-event", className: "process-action-warning" },
  { value: "CLOSED", label: "Encerrado", icon: "bi-lock", className: "process-action-neutral" },
];

function acquisitionTypeLabel(value) {
  return acquisitionTypes.find((type) => type.value === value)?.label || value || "Não informado";
}

function licitationTypeLabel(value) {
  return licitationTypes.find((type) => type.value === value)?.label || value || "Não informado";
}

function licitationStatusLabel(value) {
  return licitationResultActions.find((status) => status.value === value)?.label
    || ({ DRAFT: "Rascunho", OPEN: "Aberto", CANCELED: "Cancelado" })[value]
    || value
    || "Não informado";
}

function executionOrderTypeLabel(value) {
  return value === "SERVICE" ? "Ordem de Serviço" : "Ordem de Fornecimento";
}

function todayInputValue() {
  const today = new Date();
  const offset = today.getTimezoneOffset() * 60_000;
  return new Date(today.getTime() - offset).toISOString().slice(0, 10);
}

function pageItems(payload) {
  if (Array.isArray(payload)) return payload;
  return payload?.items || payload?.content || [];
}

function formatDate(value, withTime = false) {
  if (!value) return "Não informado";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Não informado";

  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    ...(withTime ? { hour: "2-digit", minute: "2-digit" } : {}),
  }).format(date);
}

function formatDateOnly(value) {
  if (!value) return "Não informado";
  const [year, month, day] = String(value).split("-");
  return year && month && day ? `${day}/${month}/${year}` : "Não informado";
}

function currentStep(requisition) {
  return requisition?.currentStage?.step || 1;
}

function stageState(step, requisition) {
  const activeStep = currentStep(requisition);
  if (step < activeStep) return "done";
  if (step === activeStep && requisition?.finishedAt) return "done";
  if (step === activeStep) return "current";
  return "pending";
}

function stageHistory(history, code) {
  return history.filter((item) => item.stage === code);
}

function NewProcessModal({ open, sectors, saving, onClose, onCreated }) {
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");

  useEffect(() => {
    document.body.classList.toggle("modal-open", open);
    if (open) {
      setForm(emptyForm);
      setError("");
    }
    return () => document.body.classList.remove("modal-open");
  }, [open]);

  if (!open) return null;

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    setError("");

    try {
      await onCreated({
        sectorId: form.sectorId,
        type: form.type,
        technicalDescription: form.technicalDescription.trim(),
        justification: form.justification.trim(),
        budgetAllocation: form.budgetAllocation.trim(),
        etp: { content: form.etpContent.trim() },
      });
    } catch (requestError) {
      setError(requestError.message || "Não foi possível criar a requisição.");
    }
  }

  return (
    <div className="react-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalNovoProcessoLabel">
      <div className="react-modal-card">
        <div className="modal-header" style={{ borderBottom: "var(--border1)", padding: "1.25rem 1.5rem" }}>
          <div>
            <h5 className="modal-title mb-0" id="modalNovoProcessoLabel" style={{ fontWeight: 700, color: "var(--azul-escuro)" }}>
              <i className="bi bi-plus-circle primary me-2"></i>Nova Requisição
            </h5>
            <small style={{ color: "var(--text-muted)", fontSize: "0.78rem" }}>Etapa 01 - Cadastro da Requisição</small>
          </div>
          <button type="button" className="btn-close" aria-label="Fechar" onClick={onClose} disabled={saving}></button>
        </div>

        <div className="modal-body" style={{ padding: "1.5rem" }}>
          <form id="formNovoProcesso" onSubmit={submit}>
            {error && <div className="alert alert-danger py-2">{error}</div>}

            <div className="mb-3">
              <label className="form-label" style={modalLabelStyle} htmlFor="requisitionSector">
                Secretaria / Setor de Origem
              </label>
              <select
                id="requisitionSector"
                className="form-select"
                required
                value={form.sectorId}
                onChange={(event) => updateField("sectorId", event.target.value)}
                style={modalInputStyle}
              >
                <option value="">Selecionar...</option>
                {sectors.map((sector) => (
                  <option value={sector.id} key={sector.id}>{sector.name}</option>
                ))}
              </select>
              {sectors.length === 0 && (
                <small className="text-danger">Nenhum setor disponível para cadastrar a requisição.</small>
              )}
            </div>

            <div className="mb-3">
              <label className="form-label" style={modalLabelStyle} htmlFor="requisitionType">
                Tipo de Aquisição
              </label>
              <select
                id="requisitionType"
                className="form-select"
                required
                value={form.type}
                onChange={(event) => updateField("type", event.target.value)}
                style={modalInputStyle}
              >
                <option value="">Selecionar...</option>
                {acquisitionTypes.map((type) => (
                  <option value={type.value} key={type.value}>{type.label}</option>
                ))}
              </select>
            </div>

            <div className="mb-3">
              <label className="form-label" style={modalLabelStyle} htmlFor="requisitionTechnicalDescription">
                Descrição Técnica
              </label>
              <textarea
                id="requisitionTechnicalDescription"
                className="form-control"
                rows="4"
                maxLength="5000"
                required
                value={form.technicalDescription}
                onChange={(event) => updateField("technicalDescription", event.target.value)}
                placeholder="Descreva o item ou serviço solicitado."
                style={{ ...modalInputStyle, resize: "vertical" }}
              ></textarea>
            </div>

            <div className="mb-3">
              <label className="form-label" style={modalLabelStyle} htmlFor="requisitionJustification">
                Justificativa da Necessidade
              </label>
              <textarea
                id="requisitionJustification"
                className="form-control"
                rows="3"
                maxLength="5000"
                required
                value={form.justification}
                onChange={(event) => updateField("justification", event.target.value)}
                placeholder="Informe a necessidade e a motivação da contratação."
                style={{ ...modalInputStyle, resize: "vertical" }}
              ></textarea>
            </div>

            <div className="mb-3">
              <label className="form-label" style={modalLabelStyle} htmlFor="requisitionBudgetAllocation">
                Dotação Orçamentária
              </label>
              <input
                id="requisitionBudgetAllocation"
                type="text"
                className="form-control"
                maxLength="255"
                required
                value={form.budgetAllocation}
                onChange={(event) => updateField("budgetAllocation", event.target.value)}
                placeholder="Ex: 02.004.1520.44905200.00"
                style={modalInputStyle}
              />
            </div>

            <div className="mb-1">
              <label className="form-label" style={modalLabelStyle} htmlFor="requisitionEtp">
                Estudo Técnico Preliminar (ETP)
              </label>
              <textarea
                id="requisitionEtp"
                className="form-control"
                rows="4"
                minLength="10"
                maxLength="5000"
                required
                value={form.etpContent}
                onChange={(event) => updateField("etpContent", event.target.value)}
                placeholder="Informe o conteúdo do estudo técnico preliminar."
                style={{ ...modalInputStyle, resize: "vertical" }}
              ></textarea>
            </div>
          </form>
        </div>

        <div className="modal-footer" style={{ borderTop: "var(--border1)", padding: "1rem 1.5rem", gap: "0.5rem" }}>
          <button type="button" className="btn-outline-primary" onClick={onClose} disabled={saving}>
            Cancelar
          </button>
          <button
            type="submit"
            form="formNovoProcesso"
            className="btn-primary d-flex align-items-center gap-2"
            disabled={saving || sectors.length === 0}
          >
            <i className={`bi ${saving ? "bi-arrow-repeat" : "bi-send"}`}></i>
            {saving ? "Enviando..." : "Enviar Requisição"}
          </button>
        </div>
      </div>
    </div>
  );
}

function ProcessDetails({
  requisition,
  history,
  historyLoading,
  activeStage,
  stageAction,
  actionLoading,
  actionProcessing,
  actionError,
  canProcessPurchaseStage,
  procurementEmployees,
  procurementEmployeesLoading,
  procurementAssignmentLoading,
  procurementAssignmentError,
  licitationProcess,
  licitationHistory,
  licitationHistoryLoading,
  licitationLoading,
  licitationSaving,
  licitationResultProcessing,
  licitationError,
  canApproveContractStage,
  contractStageAdvancing,
  contractStageError,
  executionOrder,
  executionOrderLoading,
  executionOrderSaving,
  executionOrderError,
  canIssueExecutionOrder,
  commitment,
  commitmentLoading,
  commitmentSaving,
  commitmentError,
  canIssueCommitment,
  paymentDeclaration,
  paymentDeclarationLoading,
  paymentDeclarationSaving,
  paymentDeclarationError,
  canIssuePaymentDeclaration,
  payment,
  paymentLoading,
  paymentSaving,
  paymentError,
  canExecutePayment,
  accountabilityReport,
  accountabilityEmployees,
  accountabilityEmployeesLoading,
  accountabilityAssignmentLoading,
  accountabilityError,
  canProcessAccountability,
  onStageChange,
  onProcessAction,
  onAssignProcurementResponsible,
  onCreateLicitationProcess,
  onPublishLicitationResult,
  onAdvanceContractStage,
  onCreateExecutionOrder,
  onCreateCommitment,
  onCreatePaymentDeclaration,
  onCreatePayment,
  onAssignAccountabilityResponsible,
}) {
  if (!requisition) {
    return (
      <div className="processo-header text-center py-5">
        <i className="bi bi-inbox fs-2 primary"></i>
        <p className="mb-0 mt-2 text-muted">Selecione uma requisição para visualizar os detalhes.</p>
      </div>
    );
  }

  return (
    <>
      <div className="processo-header">
        <div className="d-flex align-items-flex-start justify-content-between gap-2 flex-wrap">
          <h3 className="processo-titulo">{requisition.registerNumber || "Requisição sem número"}</h3>
          <button className="btn-acao" title="Imprimir" type="button" onClick={() => window.print()}>
            <i className="bi bi-printer"></i>
          </button>
        </div>

        <div className="processo-meta-grid">
          <Meta label="Setor Responsável" value={requisition.sector?.name} />
          <Meta label="Servidor Requisitante" value={requisition.responsible?.name} />
          <Meta label="Responsável de Compras" value={requisition.procurementResponsible?.name} />
          <Meta label="Tipo" value={acquisitionTypeLabel(requisition.type)} />
          <Meta label="Abertura" value={formatDate(requisition.createdAt)} />
          <Meta
            label="Etapa Atual"
            value={<span className="status-badge status-andamento">{requisition.currentStage?.description || "Não informada"}</span>}
          />
          <Meta label="Dotação Orçamentária" value={requisition.budgetAllocation} />
          <div style={{ gridColumn: "1 / -1" }}>
            <div className="meta-label">Descrição Técnica</div>
            <div className="meta-valor descricao-completa">{requisition.technicalDescription || "Não informada"}</div>
          </div>
        </div>
      </div>

      <div className="processo-card delay-1">
        <div className="stepper-wrap">
          <div className="stepper-nav">
            {workflow.map((stage, index) => {
              const step = index + 1;
              const state = stageState(step, requisition);

              return (
                <div key={stage.code}>
                  {stage.section && <div className="step-section-label ms-3">{stage.section}</div>}
                  <button
                    className={`step-btn ${activeStage === stage.code ? "active" : ""} ${state === "done" ? "concluido-btn" : ""}`}
                    type="button"
                    onClick={() => onStageChange(stage.code)}
                  >
                    <div className={`step-indicator ${state === "done" ? "concluido" : state === "current" ? "atual" : "pendente"}`}>
                      <i className={`bi ${state === "done" ? "bi-check" : state === "current" ? "bi-circle-fill" : "bi-circle"}`}></i>
                    </div>
                    <div>
                      <div className="step-label">{String(step).padStart(2, "0")}. {stage.label}</div>
                      <div className="step-sublabel">
                        {state === "done" ? "Concluída" : state === "current" ? "Etapa atual" : "Aguardando"}
                      </div>
                    </div>
                  </button>
                  {index < workflow.length - 1 && (
                    <div className={`step-connector ${state === "done" ? "concluido" : state === "current" ? "atual" : ""}`}></div>
                  )}
                </div>
              );
            })}
          </div>

          <div className="stepper-content" id="stepperContent">
            <StagePanel
              requisition={requisition}
              history={history}
              loading={historyLoading}
              code={activeStage}
              stageAction={stageAction}
              actionLoading={actionLoading}
              actionProcessing={actionProcessing}
              actionError={actionError}
              canProcessPurchaseStage={canProcessPurchaseStage}
              procurementEmployees={procurementEmployees}
              procurementEmployeesLoading={procurementEmployeesLoading}
              procurementAssignmentLoading={procurementAssignmentLoading}
              procurementAssignmentError={procurementAssignmentError}
              licitationProcess={licitationProcess}
              licitationHistory={licitationHistory}
              licitationHistoryLoading={licitationHistoryLoading}
              licitationLoading={licitationLoading}
              licitationSaving={licitationSaving}
              licitationResultProcessing={licitationResultProcessing}
              licitationError={licitationError}
              canApproveContractStage={canApproveContractStage}
              contractStageAdvancing={contractStageAdvancing}
              contractStageError={contractStageError}
              executionOrder={executionOrder}
              executionOrderLoading={executionOrderLoading}
              executionOrderSaving={executionOrderSaving}
              executionOrderError={executionOrderError}
              canIssueExecutionOrder={canIssueExecutionOrder}
              commitment={commitment}
              commitmentLoading={commitmentLoading}
              commitmentSaving={commitmentSaving}
              commitmentError={commitmentError}
              canIssueCommitment={canIssueCommitment}
              paymentDeclaration={paymentDeclaration}
              paymentDeclarationLoading={paymentDeclarationLoading}
              paymentDeclarationSaving={paymentDeclarationSaving}
              paymentDeclarationError={paymentDeclarationError}
              canIssuePaymentDeclaration={canIssuePaymentDeclaration}
              payment={payment}
              paymentLoading={paymentLoading}
              paymentSaving={paymentSaving}
              paymentError={paymentError}
              canExecutePayment={canExecutePayment}
              accountabilityReport={accountabilityReport}
              accountabilityEmployees={accountabilityEmployees}
              accountabilityEmployeesLoading={accountabilityEmployeesLoading}
              accountabilityAssignmentLoading={accountabilityAssignmentLoading}
              accountabilityError={accountabilityError}
              canProcessAccountability={canProcessAccountability}
              onProcessAction={onProcessAction}
              onAssignProcurementResponsible={onAssignProcurementResponsible}
              onCreateLicitationProcess={onCreateLicitationProcess}
              onPublishLicitationResult={onPublishLicitationResult}
              onAdvanceContractStage={onAdvanceContractStage}
              onCreateExecutionOrder={onCreateExecutionOrder}
              onCreateCommitment={onCreateCommitment}
              onCreatePaymentDeclaration={onCreatePaymentDeclaration}
              onCreatePayment={onCreatePayment}
              onAssignAccountabilityResponsible={onAssignAccountabilityResponsible}
            />
          </div>
        </div>
      </div>
    </>
  );
}

function Meta({ label, value }) {
  return (
    <div>
      <div className="meta-label">{label}</div>
      <div className="meta-valor">{value || "Não informado"}</div>
    </div>
  );
}

function StagePanel({
  requisition,
  history,
  loading,
  code,
  stageAction,
  actionLoading,
  actionProcessing,
  actionError,
  canProcessPurchaseStage,
  procurementEmployees,
  procurementEmployeesLoading,
  procurementAssignmentLoading,
  procurementAssignmentError,
  licitationProcess,
  licitationHistory,
  licitationHistoryLoading,
  licitationLoading,
  licitationSaving,
  licitationResultProcessing,
  licitationError,
  canApproveContractStage,
  contractStageAdvancing,
  contractStageError,
  executionOrder,
  executionOrderLoading,
  executionOrderSaving,
  executionOrderError,
  canIssueExecutionOrder,
  commitment,
  commitmentLoading,
  commitmentSaving,
  commitmentError,
  canIssueCommitment,
  paymentDeclaration,
  paymentDeclarationLoading,
  paymentDeclarationSaving,
  paymentDeclarationError,
  canIssuePaymentDeclaration,
  payment,
  paymentLoading,
  paymentSaving,
  paymentError,
  canExecutePayment,
  accountabilityReport,
  accountabilityEmployees,
  accountabilityEmployeesLoading,
  accountabilityAssignmentLoading,
  accountabilityError,
  canProcessAccountability,
  onProcessAction,
  onAssignProcurementResponsible,
  onCreateLicitationProcess,
  onPublishLicitationResult,
  onAdvanceContractStage,
  onCreateExecutionOrder,
  onCreateCommitment,
  onCreatePaymentDeclaration,
  onCreatePayment,
  onAssignAccountabilityResponsible,
}) {
  const stageIndex = workflow.findIndex((stage) => stage.code === code);
  const stage = workflow[stageIndex] || workflow[0];
  const step = stageIndex + 1;
  const state = stageState(step, requisition);
  const entries = stageHistory(history, stage.code);
  const displayedEntries = stage.code === "PROCESSO_LICITATORIO" ? licitationHistory : entries;
  const displayedHistoryLoading = stage.code === "PROCESSO_LICITATORIO" ? licitationHistoryLoading : loading;

  return (
    <div className="step-panel active">
      <div className="secao-titulo">
        <i className={`bi ${state === "done" ? "bi-check-circle-fill" : state === "current" ? "bi-clock-fill" : "bi-hourglass"}`}></i>
        {String(step).padStart(2, "0")} - {stage.label}
      </div>

      <div
        className="d-inline-flex align-items-center gap-2 px-3 py-2 rounded-3 mb-3"
        style={{
          background: state === "done" ? "#dcfce7" : state === "current" ? "#e8f2ff" : "var(--cinza-claro)",
          color: state === "done" ? "#16a34a" : state === "current" ? "var(--azul)" : "var(--text-muted)",
        }}
      >
        <i className={`bi ${state === "done" ? "bi-check-circle-fill" : state === "current" ? "bi-hourglass-split" : "bi-hourglass"}`}></i>
        <span style={{ fontSize: "0.82rem", fontWeight: 600 }}>
          {state === "done" ? "Etapa concluída" : state === "current" ? "Etapa atual do processo" : "Aguardando etapas anteriores"}
        </span>
      </div>

      {stage.code === "REQUISICAO_CADASTRADA" && (
        <>
          <Detail label="Responsável" value={requisition.responsible?.name} />
          <Detail label="Nº da Requisição" value={requisition.registerNumber} />
          <Detail label="Tipo de Aquisição" value={acquisitionTypeLabel(requisition.type)} />
          <Detail label="Dotação Orçamentária" value={requisition.budgetAllocation} />
          <Detail label="Justificativa" value={requisition.justification} />
          <Detail label="Estudo Técnico Preliminar" value={requisition.etp?.content} />
        </>
      )}

      {stage.code === "RECEBIMENTO_COMPRAS" && (
        <>
          <Detail label="Responsável de Compras" value={requisition.procurementResponsible?.name} />
          {stage.code === requisition.currentStage?.code && (
            <ProcurementAssignment
              employees={procurementEmployees}
              loading={procurementEmployeesLoading}
              assigning={procurementAssignmentLoading}
              error={procurementAssignmentError}
              onAssign={onAssignProcurementResponsible}
            />
          )}
        </>
      )}

      {stage.code === "COMPOSICAO_PROCESSO" && (
        <LicitationComposition
          requisition={requisition}
          employees={procurementEmployees}
          employeesLoading={procurementEmployeesLoading}
          process={licitationProcess}
          loading={licitationLoading}
          saving={licitationSaving}
          error={licitationError || procurementAssignmentError}
          editable={stage.code === requisition.currentStage?.code}
          onCreate={onCreateLicitationProcess}
        />
      )}

      {stage.code === "PROCESSO_LICITATORIO" && licitationProcess && (
        <>
          <LicitationProcessDetails process={licitationProcess} />
          {stage.code === requisition.currentStage?.code && (
            <LicitationResultActions
              process={licitationProcess}
              processing={licitationResultProcessing}
              error={licitationError}
              allowed={canProcessPurchaseStage}
              onPublish={onPublishLicitationResult}
            />
          )}
        </>
      )}

      {stage.code === "SETOR_CONTRATOS" && stage.code === requisition.currentStage?.code && (
        <ContractStageAdvance
          advancing={contractStageAdvancing}
          error={contractStageError}
          allowed={canApproveContractStage}
          onAdvance={onAdvanceContractStage}
        />
      )}

      {stage.code === "INICIO_SERVICOS" && (
        <ExecutionOrderPanel
          requisition={requisition}
          order={executionOrder}
          loading={executionOrderLoading}
          saving={executionOrderSaving}
          error={executionOrderError}
          editable={stage.code === requisition.currentStage?.code}
          allowed={canIssueExecutionOrder}
          onCreate={onCreateExecutionOrder}
        />
      )}

      {stage.code === "EMISSAO_EMPENHO" && (
        <CommitmentPanel
          requisition={requisition}
          estimatedValue={licitationProcess?.estimatedValue}
          commitment={commitment}
          loading={commitmentLoading}
          saving={commitmentSaving}
          error={commitmentError}
          editable={stage.code === requisition.currentStage?.code}
          allowed={canIssueCommitment}
          onCreate={onCreateCommitment}
        />
      )}

      {stage.code === "DECLARACAO_PAGAMENTO" && (
        <PaymentDeclarationPanel
          requisition={requisition}
          declaration={paymentDeclaration}
          loading={paymentDeclarationLoading}
          saving={paymentDeclarationSaving}
          error={paymentDeclarationError}
          editable={stage.code === requisition.currentStage?.code}
          allowed={canIssuePaymentDeclaration}
          onCreate={onCreatePaymentDeclaration}
        />
      )}

      {stage.code === "EXECUCAO_PAGAMENTO" && (
        <PaymentPanel
          requisition={requisition}
          commitment={commitment}
          payment={payment}
          loading={paymentLoading}
          saving={paymentSaving}
          error={paymentError}
          editable={stage.code === requisition.currentStage?.code}
          allowed={canExecutePayment}
          onCreate={onCreatePayment}
        />
      )}

      {stage.code === "PRESTACAO_CONTAS" && (
        <AccountabilityAssignment
          report={accountabilityReport}
          employees={accountabilityEmployees}
          employeesLoading={accountabilityEmployeesLoading}
          assigning={accountabilityAssignmentLoading}
          error={accountabilityError}
          editable={stage.code === requisition.currentStage?.code}
          onAssign={onAssignAccountabilityResponsible}
        />
      )}

      {stage.code === "HOMOLOGACAO_PRESTACAO_CONTAS" && requisition.finishedAt && (
        <FinalProcessSummary requisition={requisition} report={accountabilityReport} />
      )}

      {!requisition.finishedAt && stage.code === requisition.currentStage?.code && (analysisStages.has(stage.code) || approvalStages.has(stage.code)) && (
        <StageActions
          kind={analysisStages.has(stage.code) ? "analysis" : "approval"}
          action={stageAction}
          loading={actionLoading}
          processing={actionProcessing}
          error={actionError}
          allowed={
            (!["ANALISE_REQUISICAO", "HOMOLOGACAO_COMPRAS"].includes(stage.code) || canProcessPurchaseStage)
            && (stage.code !== "DECLARACAO_PAGAMENTO" || Boolean(paymentDeclaration))
            && (stage.code !== "ANALISE_PRESTACAO_CONTAS" || canProcessAccountability)
          }
          blockedMessage={
            stage.code === "DECLARACAO_PAGAMENTO" && !paymentDeclaration
              ? "Emita a declaração para pagamento antes de processar a homologação."
              : stage.code === "ANALISE_PRESTACAO_CONTAS" && !canProcessAccountability
                ? "Somente o responsável atribuído pode processar a análise da prestação de contas."
              : undefined
          }
          onProcess={onProcessAction}
        />
      )}

      <div className="secao-titulo mt-4 mb-2">
        <i className="bi bi-clock-history"></i> Histórico da Etapa
      </div>
      {displayedHistoryLoading && <p className="text-muted small mb-0">Carregando histórico...</p>}
      {!displayedHistoryLoading && displayedEntries.length === 0 && <p className="text-muted small mb-0">Nenhuma movimentação registrada nesta etapa.</p>}
      {!displayedHistoryLoading && displayedEntries.map((entry, index) => (
        <div className="detalhe-row" key={`${entry.changedAt}-${entry.eventType}-${index}`}>
          <div className="detalhe-chave">{formatDate(entry.changedAt, true)}</div>
          <div className="detalhe-valor">
            <strong>{entry.changedByName || "Servidor não informado"}</strong>
            <div>{entry.observation || entry.eventType}</div>
          </div>
        </div>
      ))}
    </div>
  );
}

function LicitationComposition({
  requisition,
  employees,
  employeesLoading,
  process,
  loading,
  saving,
  error,
  editable,
  onCreate,
}) {
  const [form, setForm] = useState({
    responsibleId: "",
    type: "",
    estimatedValue: "",
    objectDescription: requisition.technicalDescription || "",
    openingDate: "",
    closingDate: "",
  });

  useEffect(() => {
    setForm({
      responsibleId: "",
      type: "",
      estimatedValue: "",
      objectDescription: requisition.technicalDescription || "",
      openingDate: "",
      closingDate: "",
    });
  }, [requisition.id]);

  if (loading) {
    return <p className="text-muted small mt-3 mb-0">Carregando processo licitatório...</p>;
  }

  if (process) {
    return <LicitationProcessDetails process={process} />;
  }

  if (!editable) {
    return <p className="text-muted small mt-3 mb-0">Nenhum processo licitatório foi registrado nesta etapa.</p>;
  }

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function submit(event) {
    event.preventDefault();
    onCreate({
      requisitionId: requisition.id,
      responsibleId: form.responsibleId,
      type: form.type,
      estimatedValue: Number(form.estimatedValue),
      objectDescription: form.objectDescription.trim(),
      openingDate: form.openingDate,
      closingDate: form.closingDate,
    });
  }

  return (
    <form className="process-actions licitation-composition" onSubmit={submit}>
      <div className="secao-titulo mb-3">
        <i className="bi bi-file-earmark-ruled"></i>
        Compor Processo Licitatório
      </div>

      <div className="row g-3">
        <div className="col-12 col-md-6">
          <label className="form-label" style={modalLabelStyle}>Responsável</label>
          <select
            className="form-select process-action-observation"
            required
            value={form.responsibleId}
            onChange={(event) => updateField("responsibleId", event.target.value)}
            disabled={employeesLoading || saving}
          >
            <option value="">{employeesLoading ? "Carregando..." : "Selecionar servidor de Compras..."}</option>
            {employees.map((employee) => (
              <option value={employee.id} key={employee.id}>{employee.name}</option>
            ))}
          </select>
        </div>

        <div className="col-12 col-md-6">
          <label className="form-label" style={modalLabelStyle}>Tipo de Processo</label>
          <select
            className="form-select process-action-observation"
            required
            value={form.type}
            onChange={(event) => updateField("type", event.target.value)}
            disabled={saving}
          >
            <option value="">Selecionar modalidade...</option>
            {licitationTypes.map((type) => (
              <option value={type.value} key={type.value}>{type.label}</option>
            ))}
          </select>
        </div>

        <div className="col-12">
          <label className="form-label" style={modalLabelStyle}>Objeto do Processo</label>
          <textarea
            className="form-control process-action-observation"
            rows="3"
            minLength="10"
            maxLength="2000"
            required
            value={form.objectDescription}
            onChange={(event) => updateField("objectDescription", event.target.value)}
            disabled={saving}
          ></textarea>
        </div>

        <div className="col-12 col-md-4">
          <label className="form-label" style={modalLabelStyle}>Valor Estimado</label>
          <input
            className="form-control process-action-observation"
            type="number"
            min="0.01"
            step="0.01"
            required
            value={form.estimatedValue}
            onChange={(event) => updateField("estimatedValue", event.target.value)}
            disabled={saving}
          />
        </div>

        <div className="col-12 col-md-4">
          <label className="form-label" style={modalLabelStyle}>Data de Abertura</label>
          <input
            className="form-control process-action-observation"
            type="date"
            required
            value={form.openingDate}
            onChange={(event) => updateField("openingDate", event.target.value)}
            disabled={saving}
          />
        </div>

        <div className="col-12 col-md-4">
          <label className="form-label" style={modalLabelStyle}>Data de Encerramento</label>
          <input
            className="form-control process-action-observation"
            type="date"
            min={form.openingDate || undefined}
            required
            value={form.closingDate}
            onChange={(event) => updateField("closingDate", event.target.value)}
            disabled={saving}
          />
        </div>
      </div>

      {error && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}

      <button
        className="process-action-button process-action-success mt-3"
        type="submit"
        disabled={saving || employeesLoading || employees.length === 0}
      >
        <i className={`bi ${saving ? "bi-arrow-repeat" : "bi-file-earmark-plus"}`}></i>
        {saving ? "Criando processo..." : "Criar e enviar para licitação"}
      </button>
    </form>
  );
}

function LicitationProcessDetails({ process }) {
  const value = Number(process.estimatedValue);
  const formattedValue = Number.isFinite(value)
    ? new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value)
    : "Não informado";

  return (
    <div className="process-actions">
      <div className="secao-titulo mb-2">
        <i className="bi bi-file-earmark-check"></i>
        Processo Licitatório Criado
      </div>
      <Detail label="Número" value={process.processNumber} />
      <Detail label="Responsável" value={process.responsibleName} />
      <Detail label="Tipo de Processo" value={licitationTypeLabel(process.type)} />
      <Detail label="Status do Resultado" value={licitationStatusLabel(process.status)} />
      <Detail label="Valor Estimado" value={formattedValue} />
      <Detail label="Abertura" value={formatDateOnly(process.openingDate)} />
      <Detail label="Encerramento" value={formatDateOnly(process.closingDate)} />
      <Detail label="Objeto" value={process.objectDescription} />
      {process.winnerSupplierId && (
        <>
          <Detail label="Empresa Vencedora" value={process.winnerSupplierName} />
          <Detail label="CNPJ da Vencedora" value={process.winnerSupplierCnpj} />
        </>
      )}
    </div>
  );
}

const emptyWinnerSupplier = {
  cnpj: "",
  corporateName: "",
  tradeName: "",
  email: "",
  phone: "",
  legalRepresentative: "",
  street: "",
  number: "",
  complement: "",
  district: "",
  city: "",
  state: "",
  zipCode: "",
};

function onlyDigits(value, maxLength) {
  return value.replace(/\D/g, "").slice(0, maxLength);
}

function formatCnpjInput(value) {
  const digits = onlyDigits(value, 14);
  return digits
    .replace(/^(\d{2})(\d)/, "$1.$2")
    .replace(/^(\d{2})\.(\d{3})(\d)/, "$1.$2.$3")
    .replace(/\.(\d{3})(\d)/, ".$1/$2")
    .replace(/(\d{4})(\d)/, "$1-$2");
}

function formatPhoneInput(value) {
  const digits = onlyDigits(value, 11);
  if (digits.length <= 10) {
    return digits
      .replace(/^(\d{2})(\d)/, "($1) $2")
      .replace(/(\d{4})(\d)/, "$1-$2");
  }
  return digits
    .replace(/^(\d{2})(\d)/, "($1) $2")
    .replace(/(\d{5})(\d)/, "$1-$2");
}

function formatZipCodeInput(value) {
  return onlyDigits(value, 8).replace(/^(\d{5})(\d)/, "$1-$2");
}

function isValidCnpj(value) {
  const digits = onlyDigits(value, 14);
  if (digits.length !== 14 || /^(\d)\1+$/.test(digits)) return false;

  function checkDigit(length) {
    let factor = length - 7;
    let sum = 0;
    for (let index = 0; index < length; index += 1) {
      sum += Number(digits[index]) * factor;
      factor -= 1;
      if (factor === 1) factor = 9;
    }
    const remainder = sum % 11;
    return remainder < 2 ? 0 : 11 - remainder;
  }

  return checkDigit(12) === Number(digits[12]) && checkDigit(13) === Number(digits[13]);
}

function validateWinnerSupplier(winner, observation) {
  const errors = {};
  const requiredText = [
    ["corporateName", "Informe a razão social.", 2],
    ["tradeName", "Informe o nome fantasia.", 2],
    ["legalRepresentative", "Informe o representante legal.", 3],
    ["street", "Informe o logradouro.", 3],
    ["number", "Informe o número.", 1],
    ["district", "Informe o bairro.", 2],
    ["city", "Informe a cidade.", 2],
  ];

  if (observation.trim().length < 5) errors.observation = "Informe ao menos 5 caracteres.";
  if (!isValidCnpj(winner.cnpj)) errors.cnpj = "Informe um CNPJ válido.";
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(winner.email.trim())) errors.email = "Informe um e-mail válido.";
  if (!/^\(\d{2}\) (?:9\d{4}|\d{4})-\d{4}$/.test(winner.phone)) errors.phone = "Informe um telefone com DDD.";
  if (!/^\d{5}-\d{3}$/.test(winner.zipCode)) errors.zipCode = "Informe um CEP válido.";
  if (!/^[A-Z]{2}$/.test(winner.state)) errors.state = "Informe uma UF válida.";

  requiredText.forEach(([field, message, minLength]) => {
    if (winner[field].trim().length < minLength) errors[field] = message;
  });

  return errors;
}

function LicitationResultActions({ process, processing, error, allowed, onPublish }) {
  const [observation, setObservation] = useState("");
  const [winnerModalOpen, setWinnerModalOpen] = useState(false);
  const [winner, setWinner] = useState(emptyWinnerSupplier);
  const [touchedWinner, setTouchedWinner] = useState({});

  useEffect(() => {
    setObservation("");
    setWinnerModalOpen(false);
    setWinner(emptyWinnerSupplier);
    setTouchedWinner({});
  }, [process.id, process.status]);

  useEffect(() => {
    if (!winnerModalOpen) return undefined;

    document.body.classList.add("modal-open");
    return () => document.body.classList.remove("modal-open");
  }, [winnerModalOpen]);

  function updateWinner(field, value) {
    const formattedValue = {
      cnpj: formatCnpjInput,
      phone: formatPhoneInput,
      zipCode: formatZipCodeInput,
      state: (input) => input.replace(/[^A-Za-z]/g, "").slice(0, 2).toUpperCase(),
    }[field]?.(value) ?? value;

    setWinner((current) => ({ ...current, [field]: formattedValue }));
    setTouchedWinner((current) => ({ ...current, [field]: true }));
  }

  function publishStatus(status) {
    if (status === "FINISHED") {
      setWinnerModalOpen(true);
      return;
    }

    onPublish(status, observation.trim(), null);
  }

  function finish(event) {
    event.preventDefault();
    const errors = validateWinnerSupplier(winner, observation);
    if (Object.keys(errors).length > 0) {
      setTouchedWinner(
        Object.keys({ ...winner, observation }).reduce((fields, field) => ({ ...fields, [field]: true }), {}),
      );
      return;
    }

    onPublish("FINISHED", observation.trim(), {
      cnpj: winner.cnpj.trim(),
      corporateName: winner.corporateName.trim(),
      tradeName: winner.tradeName.trim(),
      email: winner.email.trim(),
      phone: winner.phone.trim(),
      legalRepresentative: winner.legalRepresentative.trim(),
      address: {
        street: winner.street.trim(),
        number: winner.number.trim(),
        complement: winner.complement.trim() || null,
        district: winner.district.trim(),
        city: winner.city.trim(),
        state: winner.state.trim().toUpperCase(),
        zipCode: winner.zipCode.trim(),
      },
    });
  }

  const observationValid = observation.trim().length >= 5;
  const winnerErrors = validateWinnerSupplier(winner, observation);
  const winnerValid = Object.keys(winnerErrors).length === 0;

  return (
    <div className="process-actions">
      <div className="secao-titulo mb-2">
        <i className="bi bi-megaphone"></i>
        Divulgação do Resultado
      </div>

      <input
        className="form-control"
        type="text"
        minLength="5"
        maxLength="1000"
        placeholder="Informe uma observação sobre o resultado..."
        value={observation}
        onChange={(event) => setObservation(event.target.value)}
        disabled={!allowed || Boolean(processing)}
      />

      <div className="process-action-buttons licitation-result-actions">
        {licitationResultActions.map((action) => (
          <button
            className={`process-action-button ${action.className}`}
            type="button"
            key={action.value}
            disabled={
              !allowed
              || Boolean(processing)
              || (action.value !== "FINISHED" && !observationValid)
            }
            onClick={() => publishStatus(action.value)}
          >
            <i className={`bi ${processing === action.value ? "bi-arrow-repeat" : action.icon}`}></i>
            {processing === action.value ? "Salvando..." : action.label}
          </button>
        ))}
      </div>

      {!allowed && (
        <p className="text-muted small mt-2 mb-0">
          Somente o responsável pela equipe de licitação pode divulgar o resultado.
        </p>
      )}

      {winnerModalOpen && (
        <div
          className="react-modal-backdrop"
          role="dialog"
          aria-modal="true"
          aria-labelledby="winnerSupplierModalLabel"
        >
          <div className="react-modal-card winner-supplier-modal">
            <div className="modal-header" style={{ borderBottom: "var(--border1)", padding: "1.25rem 1.5rem" }}>
              <div>
                <h5
                  className="modal-title mb-0"
                  id="winnerSupplierModalLabel"
                  style={{ fontWeight: 700, color: "var(--azul-escuro)" }}
                >
                  <i className="bi bi-building-check primary me-2"></i>
                  Empresa Vencedora
                </h5>
                <small style={{ color: "var(--text-muted)", fontSize: "0.78rem" }}>
                  Etapa 07 - Divulgação do Resultado
                </small>
              </div>
              <button
                className="btn-close"
                type="button"
                aria-label="Fechar"
                disabled={Boolean(processing)}
                onClick={() => setWinnerModalOpen(false)}
              ></button>
            </div>

            <div className="modal-body winner-supplier-modal-body" style={{ padding: "1.5rem" }}>
              <form id="winnerSupplierForm" onSubmit={finish}>
                <div className="row g-3">
                  <WinnerField
                    label="Observação da Finalização"
                    value={observation}
                    onChange={(value) => {
                      setObservation(value);
                      setTouchedWinner((current) => ({ ...current, observation: true }));
                    }}
                    error={touchedWinner.observation ? winnerErrors.observation : ""}
                    minLength="5"
                    maxLength="1000"
                    fullWidth
                  />
                  <WinnerField label="CNPJ" value={winner.cnpj} onChange={(value) => updateWinner("cnpj", value)} error={touchedWinner.cnpj ? winnerErrors.cnpj : ""} maxLength="18" inputMode="numeric" />
                  <WinnerField label="Razão Social" value={winner.corporateName} onChange={(value) => updateWinner("corporateName", value)} error={touchedWinner.corporateName ? winnerErrors.corporateName : ""} />
                  <WinnerField label="Nome Fantasia" value={winner.tradeName} onChange={(value) => updateWinner("tradeName", value)} error={touchedWinner.tradeName ? winnerErrors.tradeName : ""} />
                  <WinnerField label="E-mail" type="email" value={winner.email} onChange={(value) => updateWinner("email", value)} error={touchedWinner.email ? winnerErrors.email : ""} />
                  <WinnerField label="Telefone" value={winner.phone} onChange={(value) => updateWinner("phone", value)} error={touchedWinner.phone ? winnerErrors.phone : ""} maxLength="15" inputMode="tel" />
                  <WinnerField label="Representante Legal" value={winner.legalRepresentative} onChange={(value) => updateWinner("legalRepresentative", value)} error={touchedWinner.legalRepresentative ? winnerErrors.legalRepresentative : ""} />
                  <WinnerField label="CEP" value={winner.zipCode} onChange={(value) => updateWinner("zipCode", value)} error={touchedWinner.zipCode ? winnerErrors.zipCode : ""} maxLength="9" inputMode="numeric" />
                  <WinnerField label="Logradouro" value={winner.street} onChange={(value) => updateWinner("street", value)} error={touchedWinner.street ? winnerErrors.street : ""} />
                  <WinnerField label="Número" value={winner.number} onChange={(value) => updateWinner("number", value)} error={touchedWinner.number ? winnerErrors.number : ""} />
                  <WinnerField label="Complemento" value={winner.complement} onChange={(value) => updateWinner("complement", value)} required={false} />
                  <WinnerField label="Bairro" value={winner.district} onChange={(value) => updateWinner("district", value)} error={touchedWinner.district ? winnerErrors.district : ""} />
                  <WinnerField label="Cidade" value={winner.city} onChange={(value) => updateWinner("city", value)} error={touchedWinner.city ? winnerErrors.city : ""} />
                  <WinnerField label="UF" value={winner.state} onChange={(value) => updateWinner("state", value)} error={touchedWinner.state ? winnerErrors.state : ""} minLength="2" maxLength="2" />
                </div>

                {error && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}
              </form>
            </div>

            <div className="modal-footer" style={{ borderTop: "var(--border1)", padding: "1rem 1.5rem", gap: "0.5rem" }}>
              <button
                className="btn-outline-primary"
                type="button"
                disabled={Boolean(processing)}
                onClick={() => setWinnerModalOpen(false)}
              >
                Cancelar
              </button>
              <button
                className="btn-primary d-flex align-items-center gap-2"
                type="submit"
                form="winnerSupplierForm"
                disabled={!allowed || !observationValid || !winnerValid || Boolean(processing)}
              >
                <i className={`bi ${processing === "FINISHED" ? "bi-arrow-repeat" : "bi-check2-circle"}`}></i>
                {processing === "FINISHED" ? "Finalizando..." : "Cadastrar e finalizar"}
              </button>
            </div>
          </div>
        </div>
      )}

      {error && !winnerModalOpen && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}
    </div>
  );
}

function WinnerField({
  label,
  value,
  onChange,
  type = "text",
  required = true,
  minLength,
  maxLength = 255,
  fullWidth = false,
  error,
  inputMode,
}) {
  const hasValue = value.trim().length > 0;
  return (
    <div className={fullWidth ? "col-12" : "col-12 col-md-6"}>
      <label className="form-label" style={modalLabelStyle}>{label}</label>
      <input
        className={`form-control ${error ? "is-invalid" : hasValue ? "is-valid" : ""}`}
        type={type}
        inputMode={inputMode}
        required={required}
        minLength={minLength}
        maxLength={maxLength}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        style={modalInputStyle}
      />
      {error && <div className="invalid-feedback">{error}</div>}
    </div>
  );
}

function ContractStageAdvance({ advancing, error, allowed, onAdvance }) {
  return (
    <div className="process-actions">
      <div className="secao-titulo mb-2">
        <i className="bi bi-file-earmark-signature"></i>
        Setor de Contratos
      </div>
      <p className="text-muted small mb-3">
        A aprovação e o encaminhamento para o início dos serviços devem ser realizados pelo setor de Contratos.
      </p>
      <button
        className="process-action-button process-action-success"
        type="button"
        disabled={advancing || !allowed}
        onClick={onAdvance}
      >
        <i className={`bi ${advancing ? "bi-arrow-repeat" : "bi-arrow-right-circle"}`}></i>
        {advancing ? "Aprovando..." : "Aprovar e Avançar para Início dos Serviços"}
      </button>
      {!allowed && (
        <div className="alert alert-warning py-2 mt-3 mb-0 small">
          Somente um servidor ativo do setor de Contratos pode aprovar esta etapa.
        </div>
      )}
      {error && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}
    </div>
  );
}

function ExecutionOrderPanel({ requisition, order, loading, saving, error, editable, allowed, onCreate }) {
  const defaultType = requisition.type === "SERVICO_PJ" ? "SERVICE" : "SUPPLY";
  const [form, setForm] = useState({
    type: defaultType,
    number: `${defaultType === "SERVICE" ? "OS" : "OF"}-${requisition.registerNumber || ""}`,
    description: requisition.technicalDescription || "",
    issuedAt: todayInputValue(),
  });

  useEffect(() => {
    const type = requisition.type === "SERVICO_PJ" ? "SERVICE" : "SUPPLY";
    setForm({
      type,
      number: `${type === "SERVICE" ? "OS" : "OF"}-${requisition.registerNumber || ""}`,
      description: requisition.technicalDescription || "",
      issuedAt: todayInputValue(),
    });
  }, [requisition.id, requisition.registerNumber, requisition.technicalDescription, requisition.type]);

  function updateField(field, value) {
    setForm((current) => {
      if (field !== "type") return { ...current, [field]: value };

      const oldPrefix = current.type === "SERVICE" ? "OS-" : "OF-";
      const nextPrefix = value === "SERVICE" ? "OS-" : "OF-";
      const number = current.number.startsWith(oldPrefix)
        ? `${nextPrefix}${current.number.slice(oldPrefix.length)}`
        : current.number;

      return { ...current, type: value, number };
    });
  }

  function submit(event) {
    event.preventDefault();
    onCreate({
      type: form.type,
      number: form.number.trim(),
      description: form.description.trim(),
      issuedAt: form.issuedAt,
    });
  }

  if (loading) {
    return <p className="text-muted small mt-3 mb-0">Carregando ordem de execução...</p>;
  }

  if (order) {
    return (
      <div className="process-actions">
        <div className="secao-titulo mb-2">
          <i className="bi bi-file-earmark-check"></i>
          Ordem de Execução Emitida
        </div>
        <Detail label="Tipo" value={executionOrderTypeLabel(order.type)} />
        <Detail label="Número" value={order.number} />
        <Detail label="Emitida por" value={order.issuedByName} />
        <Detail label="Data de Emissão" value={formatDateOnly(order.issuedAt)} />
        <Detail label="Descrição" value={order.description} />
      </div>
    );
  }

  if (!editable) {
    return <p className="text-muted small mt-3 mb-0">Nenhuma ordem de execução foi emitida nesta etapa.</p>;
  }

  return (
    <form className="process-actions" onSubmit={submit}>
      <div className="secao-titulo mb-3">
        <i className="bi bi-play-circle"></i>
        Início dos Serviços
      </div>

      <div className="row g-3">
        <div className="col-12 col-md-6">
          <label className="form-label" style={modalLabelStyle}>Tipo da Ordem</label>
          <select
            className="form-select"
            required
            value={form.type}
            onChange={(event) => updateField("type", event.target.value)}
            disabled={saving || !allowed}
            style={modalInputStyle}
          >
            <option value="SUPPLY">Ordem de Fornecimento</option>
            <option value="SERVICE">Ordem de Serviço</option>
          </select>
        </div>

        <div className="col-12 col-md-6">
          <label className="form-label" style={modalLabelStyle}>Número da Ordem</label>
          <input
            className="form-control"
            type="text"
            required
            maxLength="100"
            value={form.number}
            onChange={(event) => updateField("number", event.target.value)}
            disabled={saving || !allowed}
            style={modalInputStyle}
          />
        </div>

        <div className="col-12">
          <label className="form-label" style={modalLabelStyle}>Descrição</label>
          <input
            className="form-control"
            type="text"
            required
            minLength="10"
            maxLength="2000"
            value={form.description}
            onChange={(event) => updateField("description", event.target.value)}
            disabled={saving || !allowed}
            style={modalInputStyle}
          />
        </div>

        <div className="col-12 col-md-6">
          <label className="form-label" style={modalLabelStyle}>Data de Emissão</label>
          <input
            className="form-control"
            type="date"
            required
            value={form.issuedAt}
            onChange={(event) => updateField("issuedAt", event.target.value)}
            disabled={saving || !allowed}
            style={modalInputStyle}
          />
        </div>
      </div>

      {!allowed && (
        <p className="text-danger small mt-3 mb-0">
          Somente o servidor responsável pela requisição pode emitir a ordem.
        </p>
      )}
      {error && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}

      <button
        className="process-action-button process-action-success mt-3"
        type="submit"
        disabled={saving || !allowed}
      >
        <i className={`bi ${saving ? "bi-arrow-repeat" : "bi-send-check"}`}></i>
        {saving ? "Emitindo..." : "Emitir ordem e avançar"}
      </button>
    </form>
  );
}

function CommitmentPanel({
  requisition,
  estimatedValue,
  commitment,
  loading,
  saving,
  error,
  editable,
  allowed,
  onCreate,
}) {
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState({
    type: "ORDINARY",
    commitmentNumber: `EMP-${requisition.registerNumber || ""}`,
    reservedValue: estimatedValue || "",
  });

  useEffect(() => {
    setModalOpen(false);
    setForm({
      type: "ORDINARY",
      commitmentNumber: `EMP-${requisition.registerNumber || ""}`,
      reservedValue: estimatedValue || "",
    });
  }, [estimatedValue, requisition.id, requisition.registerNumber]);

  useEffect(() => {
    if (!modalOpen) return undefined;

    document.body.classList.add("modal-open");
    return () => document.body.classList.remove("modal-open");
  }, [modalOpen]);

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function submit(event) {
    event.preventDefault();
    onCreate({
      type: form.type,
      commitmentNumber: form.commitmentNumber.trim(),
      reservedValue: Number(form.reservedValue),
    });
  }

  if (loading) {
    return <p className="text-muted small mt-3 mb-0">Carregando empenho...</p>;
  }

  if (commitment) {
    const value = Number(commitment.reservedValue);
    const formattedValue = Number.isFinite(value)
      ? new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value)
      : "Não informado";

    return (
      <div className="process-actions">
        <div className="secao-titulo mb-2">
          <i className="bi bi-receipt-cutoff"></i>
          Empenho Emitido
        </div>
        <Detail label="Número" value={commitment.commitmentNumber} />
        <Detail label="Tipo" value={{
          ORDINARY: "Ordinário",
          ESTIMATED: "Estimativo",
          GLOBAL: "Global",
        }[commitment.type]} />
        <Detail label="Valor Reservado" value={formattedValue} />
        <Detail label="Emitido por" value={commitment.issuedByName} />
        <Detail label="Ordem Vinculada" value={commitment.executionOrderNumber} />
      </div>
    );
  }

  if (!editable) {
    return <p className="text-muted small mt-3 mb-0">Nenhum empenho foi emitido nesta etapa.</p>;
  }

  return (
    <div className="process-actions">
      <div className="secao-titulo mb-2">
        <i className="bi bi-receipt"></i>
        Emissão de Empenho
      </div>
      <p className="text-muted small mb-3">
        Preencha os dados orçamentários para reservar o valor e avançar o processo.
      </p>
      <button
        className="process-action-button process-action-success"
        type="button"
        disabled={!allowed || saving}
        onClick={() => setModalOpen(true)}
      >
        <i className="bi bi-pencil-square"></i>
        Preencher Empenho
      </button>

      {!allowed && (
        <p className="text-danger small mt-2 mb-0">
          Somente o servidor responsável pela requisição pode emitir o empenho.
        </p>
      )}
      {error && !modalOpen && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}

      {modalOpen && (
        <div
          className="react-modal-backdrop"
          role="dialog"
          aria-modal="true"
          aria-labelledby="commitmentModalLabel"
        >
          <div className="react-modal-card commitment-modal">
            <div className="modal-header" style={{ borderBottom: "var(--border1)", padding: "1.25rem 1.5rem" }}>
              <div>
                <h5
                  className="modal-title mb-0"
                  id="commitmentModalLabel"
                  style={{ fontWeight: 700, color: "var(--azul-escuro)" }}
                >
                  <i className="bi bi-receipt primary me-2"></i>
                  Emissão de Empenho
                </h5>
                <small style={{ color: "var(--text-muted)", fontSize: "0.78rem" }}>
                  Etapa 10 - Reserva Orçamentária
                </small>
              </div>
              <button
                className="btn-close"
                type="button"
                aria-label="Fechar"
                disabled={saving}
                onClick={() => setModalOpen(false)}
              ></button>
            </div>

            <div className="modal-body" style={{ padding: "1.5rem" }}>
              <form id="commitmentForm" onSubmit={submit}>
                <div className="mb-3">
                  <label className="form-label" style={modalLabelStyle}>Tipo do Empenho</label>
                  <select
                    className="form-select"
                    required
                    value={form.type}
                    onChange={(event) => updateField("type", event.target.value)}
                    disabled={saving}
                    style={modalInputStyle}
                  >
                    <option value="ORDINARY">Ordinário</option>
                    <option value="ESTIMATED">Estimativo</option>
                    <option value="GLOBAL">Global</option>
                  </select>
                </div>

                <div className="mb-3">
                  <label className="form-label" style={modalLabelStyle}>Número do Empenho</label>
                  <input
                    className="form-control"
                    type="text"
                    required
                    maxLength="100"
                    value={form.commitmentNumber}
                    onChange={(event) => updateField("commitmentNumber", event.target.value)}
                    disabled={saving}
                    style={modalInputStyle}
                  />
                </div>

                <div>
                  <label className="form-label" style={modalLabelStyle}>Valor Reservado</label>
                  <input
                    className="form-control"
                    type="number"
                    required
                    min="0.01"
                    step="0.01"
                    value={form.reservedValue}
                    onChange={(event) => updateField("reservedValue", event.target.value)}
                    disabled={saving}
                    style={modalInputStyle}
                  />
                </div>

                {error && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}
              </form>
            </div>

            <div className="modal-footer" style={{ borderTop: "var(--border1)", padding: "1rem 1.5rem", gap: "0.5rem" }}>
              <button
                className="btn-outline-primary"
                type="button"
                disabled={saving}
                onClick={() => setModalOpen(false)}
              >
                Cancelar
              </button>
              <button
                className="btn-primary d-flex align-items-center gap-2"
                type="submit"
                form="commitmentForm"
                disabled={saving || !allowed}
              >
                <i className={`bi ${saving ? "bi-arrow-repeat" : "bi-send-check"}`}></i>
                {saving ? "Emitindo..." : "Emitir e avançar"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function PaymentDeclarationPanel({
  requisition,
  declaration,
  loading,
  saving,
  error,
  editable,
  allowed,
  onCreate,
}) {
  const defaultType = requisition.type === "SERVICO_PJ" ? "SERVICE_MEASUREMENT" : "ITEM_RECEIPT";
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState({
    type: defaultType,
    description: requisition.technicalDescription || "",
  });

  useEffect(() => {
    const type = requisition.type === "SERVICO_PJ" ? "SERVICE_MEASUREMENT" : "ITEM_RECEIPT";
    setModalOpen(false);
    setForm({
      type,
      description: requisition.technicalDescription || "",
    });
  }, [requisition.id, requisition.technicalDescription, requisition.type]);

  useEffect(() => {
    if (!modalOpen) return undefined;

    document.body.classList.add("modal-open");
    return () => document.body.classList.remove("modal-open");
  }, [modalOpen]);

  function submit(event) {
    event.preventDefault();
    onCreate({
      type: form.type,
      description: form.description.trim(),
    });
  }

  if (loading) {
    return <p className="text-muted small mt-3 mb-0">Carregando declaração para pagamento...</p>;
  }

  if (declaration) {
    const typeLabel = {
      ITEM_RECEIPT: "Recebimento de Itens",
      SERVICE_MEASUREMENT: "Medição de Serviços",
      PAYMENT_AUTHORIZATION: "Autorização de Pagamento",
    }[declaration.type];

    return (
      <div className="process-actions">
        <div className="secao-titulo mb-2">
          <i className="bi bi-file-earmark-check"></i>
          Declaração para Pagamento Emitida
        </div>
        <Detail label="Tipo" value={typeLabel} />
        <Detail label="Empenho" value={declaration.commitmentNumber} />
        <Detail label="Emitida por" value={declaration.approvedByName} />
        <Detail label="Descrição" value={declaration.description} />
        <Detail
          label="Aprovação do Secretário"
          value={declaration.secretaryApproved ? "Aprovada" : "Pendente"}
        />
      </div>
    );
  }

  if (!editable) {
    return <p className="text-muted small mt-3 mb-0">Nenhuma declaração para pagamento foi emitida nesta etapa.</p>;
  }

  return (
    <div className="process-actions">
      <div className="secao-titulo mb-2">
        <i className="bi bi-file-earmark-text"></i>
        Emissão da Declaração para Pagamento
      </div>
      <p className="text-muted small mb-3">
        Registre o recebimento ou a medição para encaminhar a declaração à homologação.
      </p>
      <button
        className="process-action-button process-action-success"
        type="button"
        disabled={!allowed || saving}
        onClick={() => setModalOpen(true)}
      >
        <i className="bi bi-pencil-square"></i>
        Preencher Declaração
      </button>

      {!allowed && (
        <p className="text-danger small mt-2 mb-0">
          Somente o servidor responsável pela requisição pode emitir a declaração.
        </p>
      )}
      {error && !modalOpen && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}

      {modalOpen && (
        <div
          className="react-modal-backdrop"
          role="dialog"
          aria-modal="true"
          aria-labelledby="paymentDeclarationModalLabel"
        >
          <div className="react-modal-card commitment-modal">
            <div className="modal-header" style={{ borderBottom: "var(--border1)", padding: "1.25rem 1.5rem" }}>
              <div>
                <h5
                  className="modal-title mb-0"
                  id="paymentDeclarationModalLabel"
                  style={{ fontWeight: 700, color: "var(--azul-escuro)" }}
                >
                  <i className="bi bi-file-earmark-text primary me-2"></i>
                  Declaração para Pagamento
                </h5>
                <small style={{ color: "var(--text-muted)", fontSize: "0.78rem" }}>
                  Etapa 11 - Recebimento ou Medição
                </small>
              </div>
              <button
                className="btn-close"
                type="button"
                aria-label="Fechar"
                disabled={saving}
                onClick={() => setModalOpen(false)}
              ></button>
            </div>

            <div className="modal-body" style={{ padding: "1.5rem" }}>
              <form id="paymentDeclarationForm" onSubmit={submit}>
                <div className="mb-3">
                  <label className="form-label" style={modalLabelStyle}>Tipo da Declaração</label>
                  <select
                    className="form-select"
                    required
                    value={form.type}
                    onChange={(event) => setForm((current) => ({ ...current, type: event.target.value }))}
                    disabled={saving}
                    style={modalInputStyle}
                  >
                    <option value="ITEM_RECEIPT">Recebimento de Itens</option>
                    <option value="SERVICE_MEASUREMENT">Medição de Serviços</option>
                    <option value="PAYMENT_AUTHORIZATION">Autorização de Pagamento</option>
                  </select>
                </div>

                <div>
                  <label className="form-label" style={modalLabelStyle}>Descrição</label>
                  <input
                    className="form-control"
                    type="text"
                    required
                    minLength="10"
                    maxLength="2000"
                    value={form.description}
                    onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
                    disabled={saving}
                    style={modalInputStyle}
                  />
                </div>

                {error && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}
              </form>
            </div>

            <div className="modal-footer" style={{ borderTop: "var(--border1)", padding: "1rem 1.5rem", gap: "0.5rem" }}>
              <button
                className="btn-outline-primary"
                type="button"
                disabled={saving}
                onClick={() => setModalOpen(false)}
              >
                Cancelar
              </button>
              <button
                className="btn-primary d-flex align-items-center gap-2"
                type="submit"
                form="paymentDeclarationForm"
                disabled={saving || !allowed}
              >
                <i className={`bi ${saving ? "bi-arrow-repeat" : "bi-send-check"}`}></i>
                {saving ? "Emitindo..." : "Emitir declaração"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function PaymentPanel({
  requisition,
  commitment,
  payment,
  loading,
  saving,
  error,
  editable,
  allowed,
  onCreate,
}) {
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState({
    value: commitment?.reservedValue || "",
    paidAt: todayInputValue(),
  });

  useEffect(() => {
    setModalOpen(false);
    setForm({
      value: commitment?.reservedValue || "",
      paidAt: todayInputValue(),
    });
  }, [commitment?.reservedValue, requisition.id]);

  useEffect(() => {
    if (!modalOpen) return undefined;

    document.body.classList.add("modal-open");
    return () => document.body.classList.remove("modal-open");
  }, [modalOpen]);

  function submit(event) {
    event.preventDefault();
    onCreate({
      value: Number(form.value),
      paidAt: form.paidAt,
    });
  }

  if (loading) {
    return <p className="text-muted small mt-3 mb-0">Carregando execução do pagamento...</p>;
  }

  if (payment) {
    const value = Number(payment.value);
    const formattedValue = Number.isFinite(value)
      ? new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value)
      : "Não informado";

    return (
      <div className="process-actions">
        <div className="secao-titulo mb-2">
          <i className="bi bi-cash-coin"></i>
          Pagamento Executado
        </div>
        <Detail label="Valor Pago" value={formattedValue} />
        <Detail label="Data do Pagamento" value={formatDateOnly(payment.paidAt)} />
        <Detail label="Responsável da Fazenda" value={payment.treasuryResponsibleName} />
        <Detail label="Setor da Fazenda" value={payment.treasurySectorName} />
        <Detail label="Aprovação da Fazenda" value={payment.treasuryApproved ? "Aprovado" : "Pendente"} />
      </div>
    );
  }

  if (!editable) {
    return <p className="text-muted small mt-3 mb-0">Nenhum pagamento foi executado nesta etapa.</p>;
  }

  return (
    <div className="process-actions">
      <div className="secao-titulo mb-2">
        <i className="bi bi-bank"></i>
        Execução do Pagamento
      </div>
      <p className="text-muted small mb-3">
        Registre a execução financeira para encaminhar o processo à prestação de contas.
      </p>
      <button
        className="process-action-button process-action-success"
        type="button"
        disabled={!allowed || saving}
        onClick={() => setModalOpen(true)}
      >
        <i className="bi bi-pencil-square"></i>
        Preencher Pagamento
      </button>

      {!allowed && (
        <p className="text-danger small mt-2 mb-0">
          É necessário estar autenticado como servidor da Fazenda para executar o pagamento.
        </p>
      )}
      {error && !modalOpen && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}

      {modalOpen && (
        <div
          className="react-modal-backdrop"
          role="dialog"
          aria-modal="true"
          aria-labelledby="paymentModalLabel"
        >
          <div className="react-modal-card commitment-modal">
            <div className="modal-header" style={{ borderBottom: "var(--border1)", padding: "1.25rem 1.5rem" }}>
              <div>
                <h5
                  className="modal-title mb-0"
                  id="paymentModalLabel"
                  style={{ fontWeight: 700, color: "var(--azul-escuro)" }}
                >
                  <i className="bi bi-cash-coin primary me-2"></i>
                  Execução do Pagamento
                </h5>
                <small style={{ color: "var(--text-muted)", fontSize: "0.78rem" }}>
                  Etapa 12 - Secretaria da Fazenda
                </small>
              </div>
              <button
                className="btn-close"
                type="button"
                aria-label="Fechar"
                disabled={saving}
                onClick={() => setModalOpen(false)}
              ></button>
            </div>

            <div className="modal-body" style={{ padding: "1.5rem" }}>
              <form id="paymentForm" onSubmit={submit}>
                <div className="mb-3">
                  <label className="form-label" style={modalLabelStyle}>Valor do Pagamento</label>
                  <input
                    className="form-control"
                    type="number"
                    min="0.01"
                    step="0.01"
                    required
                    value={form.value}
                    onChange={(event) => setForm((current) => ({ ...current, value: event.target.value }))}
                    disabled={saving}
                    style={modalInputStyle}
                  />
                </div>

                <div>
                  <label className="form-label" style={modalLabelStyle}>Data do Pagamento</label>
                  <input
                    className="form-control"
                    type="date"
                    max={todayInputValue()}
                    required
                    value={form.paidAt}
                    onChange={(event) => setForm((current) => ({ ...current, paidAt: event.target.value }))}
                    disabled={saving}
                    style={modalInputStyle}
                  />
                </div>

                {error && <div className="alert alert-danger py-2 mt-3 mb-0 small">{error}</div>}
              </form>
            </div>

            <div className="modal-footer" style={{ borderTop: "var(--border1)", padding: "1rem 1.5rem", gap: "0.5rem" }}>
              <button
                className="btn-outline-primary"
                type="button"
                disabled={saving}
                onClick={() => setModalOpen(false)}
              >
                Cancelar
              </button>
              <button
                className="btn-primary d-flex align-items-center gap-2"
                type="submit"
                form="paymentForm"
                disabled={saving || !allowed}
              >
                <i className={`bi ${saving ? "bi-arrow-repeat" : "bi-send-check"}`}></i>
                {saving ? "Executando..." : "Executar e avançar"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function FinalProcessSummary({ requisition, report }) {
  const approved = requisition.requestStatus === "APROVADA";

  return (
    <div className="process-actions">
      <div className="secao-titulo mb-2">
        <i className={`bi ${approved ? "bi-patch-check-fill" : "bi-x-octagon-fill"}`}></i>
        {approved ? "Processo Concluído" : "Processo Reprovado"}
      </div>
      <div
        className={`alert ${approved ? "alert-success" : "alert-danger"} py-3 mb-3`}
        role="status"
      >
        <strong>
          {approved
            ? "A prestação de contas foi homologada e o processo foi encerrado."
            : "A prestação de contas foi reprovada e o processo foi encerrado."}
        </strong>
      </div>
      <Detail label="Resultado Final" value={approved ? "Homologado" : "Reprovado"} />
      <Detail label="Responsável pela Prestação" value={report?.responsibleName} />
      <Detail label="Contrato" value={report?.contractNumber} />
      <Detail label="Encerramento" value={formatDate(requisition.finishedAt, true)} />
    </div>
  );
}

function AccountabilityAssignment({
  report,
  employees,
  employeesLoading,
  assigning,
  error,
  editable,
  onAssign,
}) {
  const [employeeId, setEmployeeId] = useState("");

  useEffect(() => {
    setEmployeeId("");
  }, [employees]);

  if (report) {
    return (
      <div className="process-actions">
        <div className="secao-titulo mb-2">
          <i className="bi bi-person-check"></i>
          Responsável pela Prestação de Contas
        </div>
        <Detail label="Responsável pela Análise" value={report.responsibleName} />
        <Detail label="Contrato" value={report.contractNumber} />
        <Detail
          label="Situação"
          value={{
            UNDER_REVIEW: "Em análise",
            PENDING: "Pendente",
            UNDER_DILIGENCE: "Em diligência",
            REJECTED: "Reprovada",
            APPROVED: "Aprovada",
          }[report.status] || report.status}
        />
      </div>
    );
  }

  if (!editable) {
    return <p className="text-muted small mt-3 mb-0">Nenhum responsável foi atribuído à prestação de contas.</p>;
  }

  return (
    <div className="process-actions">
      <div className="secao-titulo mb-2">
        <i className="bi bi-person-badge"></i>
        Definir Responsável pela Prestação de Contas
      </div>
      <p className="text-muted small mb-3">
        O funcionário selecionado será o único autorizado a validar a fase 14.
      </p>

      <div className="d-flex align-items-center gap-2 flex-wrap">
        <select
          className="form-select process-action-observation"
          value={employeeId}
          onChange={(event) => setEmployeeId(event.target.value)}
          disabled={employeesLoading || assigning}
        >
          <option value="">
            {employeesLoading ? "Carregando funcionários..." : "Selecione um funcionário"}
          </option>
          {employees.map((employee) => (
            <option value={employee.id} key={employee.id}>{employee.name}</option>
          ))}
        </select>

        <button
          className="process-action-button process-action-success"
          type="button"
          disabled={!employeeId || employeesLoading || assigning}
          onClick={() => onAssign(employeeId)}
        >
          <i className={`bi ${assigning ? "bi-arrow-repeat" : "bi-send-check"}`}></i>
          {assigning ? "Atribuindo..." : "Atribuir e avançar"}
        </button>
      </div>

      {error && <div className="alert alert-danger py-2 mt-2 mb-0 small">{error}</div>}
      {!employeesLoading && employees.length === 0 && !error && (
        <p className="text-muted small mt-2 mb-0">
          Nenhum funcionário ativo com permissão de prestação de contas foi encontrado.
        </p>
      )}
    </div>
  );
}

function ProcurementAssignment({ employees, loading, assigning, error, onAssign }) {
  const [employeeId, setEmployeeId] = useState("");

  useEffect(() => {
    setEmployeeId("");
  }, [employees]);

  return (
    <div className="process-actions">
      <div className="secao-titulo mb-2">
        <i className="bi bi-person-check"></i>
        Definir Responsável de Compras
      </div>

      <div className="process-assignment-row">
        <select
          className="form-select process-action-observation"
          value={employeeId}
          onChange={(event) => setEmployeeId(event.target.value)}
          disabled={loading || assigning}
        >
          <option value="">{loading ? "Carregando servidores..." : "Selecionar servidor de Compras..."}</option>
          {employees.map((employee) => (
            <option value={employee.id} key={employee.id}>{employee.name}</option>
          ))}
        </select>

        <button
          className="process-action-button process-action-success"
          type="button"
          disabled={!employeeId || loading || assigning}
          onClick={() => onAssign(employeeId)}
        >
          <i className={`bi ${assigning ? "bi-arrow-repeat" : "bi-send-check"}`}></i>
          {assigning ? "Atribuindo..." : "Atribuir e enviar"}
        </button>
      </div>

      {error && <div className="alert alert-danger py-2 mt-2 mb-0 small">{error}</div>}
      {!loading && employees.length === 0 && !error && (
        <p className="text-muted small mt-2 mb-0">Nenhum servidor ativo foi encontrado no setor de Compras.</p>
      )}
      <p className="text-muted small mt-2 mb-0">
        O servidor selecionado será o único autorizado a processar a análise e a homologação de Compras.
      </p>
    </div>
  );
}

function StageActions({ kind, action, loading, processing, error, allowed, blockedMessage, onProcess }) {
  const [observation, setObservation] = useState("");
  const actions = kind === "analysis" ? analysisActions : approvalActions;

  useEffect(() => {
    setObservation("");
  }, [action?.id]);

  return (
    <div className="process-actions">
      <div className="secao-titulo mb-2">
        <i className={`bi ${kind === "analysis" ? "bi-clipboard-check" : "bi-patch-check"}`}></i>
        {kind === "analysis" ? "Resultado da Análise" : "Homologação"}
      </div>

      <textarea
        className="form-control process-action-observation"
        rows="2"
        maxLength="1000"
        value={observation}
        onChange={(event) => setObservation(event.target.value)}
        placeholder="Observação opcional sobre a decisão"
        disabled={loading || processing || !action || !allowed}
      ></textarea>

      {error && <div className="alert alert-danger py-2 mt-2 mb-0 small">{error}</div>}

      <div className={`process-action-buttons ${kind === "approval" ? "approval-actions" : ""}`}>
        {actions.map((item) => (
          <button
            className={`process-action-button ${item.className}`}
            type="button"
            key={item.value}
            disabled={loading || processing || !action || !allowed}
            onClick={() => onProcess(item.value, observation.trim() || null)}
          >
            <i className={`bi ${processing === item.value ? "bi-arrow-repeat" : item.icon}`}></i>
            {processing === item.value ? "Processando..." : item.label}
          </button>
        ))}
      </div>

      {loading && <p className="text-muted small mt-2 mb-0">Carregando ação pendente...</p>}
      {!allowed && (
        <p className="text-danger small mt-2 mb-0">
          {blockedMessage || "Somente o responsável de Compras atribuído pode processar esta etapa."}
        </p>
      )}
      {!loading && !action && !error && (
        <p className="text-muted small mt-2 mb-0">Não há ação pendente para esta etapa.</p>
      )}
    </div>
  );
}

function Detail({ label, value }) {
  return (
    <div className="detalhe-row">
      <div className="detalhe-chave">{label}</div>
      <div className="detalhe-valor">{value || "Não informado"}</div>
    </div>
  );
}

const modalLabelStyle = {
  fontSize: "0.82rem",
  fontWeight: 600,
  color: "var(--text-muted)",
  textTransform: "uppercase",
  letterSpacing: "0.6px",
};

const modalInputStyle = {
  border: "var(--border1-5)",
  borderRadius: "8px",
  fontSize: "0.9rem",
};

export default function ProcessesPage() {
  const [requisitions, setRequisitions] = useState([]);
  const [sectors, setSectors] = useState([]);
  const [history, setHistory] = useState([]);
  const [activeRequisitionId, setActiveRequisitionId] = useState(null);
  const [activeStage, setActiveStage] = useState(workflow[0].code);
  const [listSearch, setListSearch] = useState("");
  const [page, setPage] = useState(0);
  const [pagination, setPagination] = useState({ totalElements: 0, totalPages: 0, last: true });
  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [stageAction, setStageAction] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [actionProcessing, setActionProcessing] = useState("");
  const [actionError, setActionError] = useState("");
  const [procurementEmployees, setProcurementEmployees] = useState([]);
  const [procurementEmployeesLoading, setProcurementEmployeesLoading] = useState(false);
  const [procurementAssignmentLoading, setProcurementAssignmentLoading] = useState(false);
  const [procurementAssignmentError, setProcurementAssignmentError] = useState("");
  const [authenticatedEmployeeId, setAuthenticatedEmployeeId] = useState(null);
  const [authenticatedEmployeeSector, setAuthenticatedEmployeeSector] = useState("");
  const [licitationProcess, setLicitationProcess] = useState(null);
  const [licitationHistory, setLicitationHistory] = useState([]);
  const [licitationHistoryLoading, setLicitationHistoryLoading] = useState(false);
  const [licitationLoading, setLicitationLoading] = useState(false);
  const [licitationSaving, setLicitationSaving] = useState(false);
  const [licitationResultProcessing, setLicitationResultProcessing] = useState("");
  const [licitationError, setLicitationError] = useState("");
  const [contractStageAdvancing, setContractStageAdvancing] = useState(false);
  const [contractStageError, setContractStageError] = useState("");
  const [executionOrder, setExecutionOrder] = useState(null);
  const [executionOrderLoading, setExecutionOrderLoading] = useState(false);
  const [executionOrderSaving, setExecutionOrderSaving] = useState(false);
  const [executionOrderError, setExecutionOrderError] = useState("");
  const [commitment, setCommitment] = useState(null);
  const [commitmentLoading, setCommitmentLoading] = useState(false);
  const [commitmentSaving, setCommitmentSaving] = useState(false);
  const [commitmentError, setCommitmentError] = useState("");
  const [paymentDeclaration, setPaymentDeclaration] = useState(null);
  const [paymentDeclarationLoading, setPaymentDeclarationLoading] = useState(false);
  const [paymentDeclarationSaving, setPaymentDeclarationSaving] = useState(false);
  const [paymentDeclarationError, setPaymentDeclarationError] = useState("");
  const [payment, setPayment] = useState(null);
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [paymentSaving, setPaymentSaving] = useState(false);
  const [paymentError, setPaymentError] = useState("");
  const [accountabilityReport, setAccountabilityReport] = useState(null);
  const [accountabilityEmployees, setAccountabilityEmployees] = useState([]);
  const [accountabilityEmployeesLoading, setAccountabilityEmployeesLoading] = useState(false);
  const [accountabilityAssignmentLoading, setAccountabilityAssignmentLoading] = useState(false);
  const [accountabilityError, setAccountabilityError] = useState("");
  const [error, setError] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [licitationHistoryRefreshKey, setLicitationHistoryRefreshKey] = useState(0);
  const deferredSearch = useDeferredValue(listSearch);

  useEffect(() => {
    let cancelled = false;

    async function loadPage() {
      setLoading(true);
      setError("");

      try {
        const response = await biddingApi.getRequisitions(page, PAGE_SIZE);
        if (cancelled) return;

        const items = pageItems(response);
        setRequisitions(items);
        setPagination({
          totalElements: response?.totalElements ?? items.length,
          totalPages: response?.totalPages ?? (items.length ? 1 : 0),
          last: response?.last ?? true,
        });
        setActiveRequisitionId((current) => (
          items.some((item) => item.id === current) ? current : items[0]?.id || null
        ));
      } catch (requestError) {
        if (!cancelled) {
          setRequisitions([]);
          setActiveRequisitionId(null);
          setError(requestError.message || "Não foi possível carregar as requisições.");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadPage();
    return () => {
      cancelled = true;
    };
  }, [page, refreshKey]);

  useEffect(() => {
    let cancelled = false;

    Promise.allSettled([api.getSectors(), api.getEmployeeDetails()])
      .then(([sectorsResult, employeeResult]) => {
        if (cancelled) return;

        setSectors(sectorsResult.status === "fulfilled" ? pageItems(sectorsResult.value) : []);
        setAuthenticatedEmployeeId(employeeResult.status === "fulfilled" ? employeeResult.value?.id || null : null);
        setAuthenticatedEmployeeSector(employeeResult.status === "fulfilled" ? employeeResult.value?.sector || "" : "");
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const activeRequisition = requisitions.find((item) => item.id === activeRequisitionId) || null;
  const canProcessPurchaseStage = Boolean(
    authenticatedEmployeeId
      && activeRequisition?.procurementResponsible?.id
      && String(authenticatedEmployeeId) === String(activeRequisition.procurementResponsible.id),
  );
  const canManageLicitationProcess = Boolean(
    authenticatedEmployeeId
      && licitationProcess?.responsibleId
      && String(authenticatedEmployeeId) === String(licitationProcess.responsibleId),
  );
  const canApproveContractStage = authenticatedEmployeeSector
    .toLocaleLowerCase("pt-BR")
    .includes("contrat");
  const canIssueExecutionOrder = Boolean(
    authenticatedEmployeeId
      && activeRequisition?.responsible?.id
      && String(authenticatedEmployeeId) === String(activeRequisition.responsible.id),
  );
  const canIssueCommitment = canIssueExecutionOrder;
  const canIssuePaymentDeclaration = canIssueExecutionOrder;
  const canExecutePayment = Boolean(authenticatedEmployeeId);
  const canProcessAccountability = Boolean(
    authenticatedEmployeeId
      && accountabilityReport?.responsibleId
      && String(authenticatedEmployeeId) === String(accountabilityReport.responsibleId),
  );

  useEffect(() => {
    if (!activeRequisition) {
      setHistory([]);
      return;
    }

    let cancelled = false;
    setActiveStage(activeRequisition.currentStage?.code || workflow[0].code);
    setHistory([]);
    setHistoryLoading(true);

    biddingApi.getRequisitionHistory(activeRequisition.id)
      .then((response) => {
        if (!cancelled) setHistory(Array.isArray(response) ? response : []);
      })
      .catch(() => {
        if (!cancelled) setHistory([]);
      })
      .finally(() => {
        if (!cancelled) setHistoryLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [activeRequisition?.id, activeRequisition?.currentStage?.code]);

  useEffect(() => {
    if (!activeRequisition || currentStep(activeRequisition) < 9) {
      setExecutionOrder(null);
      setExecutionOrderError("");
      setExecutionOrderLoading(false);
      return;
    }

    let cancelled = false;
    setExecutionOrder(null);
    setExecutionOrderError("");
    setExecutionOrderLoading(true);

    biddingApi.getExecutionOrderByRequisition(activeRequisition.id)
      .then((response) => {
        if (!cancelled) setExecutionOrder(response);
      })
      .catch((requestError) => {
        if (cancelled || requestError.status === 404) return;
        setExecutionOrderError(requestError.message || "Não foi possível carregar a ordem de execução.");
      })
      .finally(() => {
        if (!cancelled) setExecutionOrderLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [activeRequisition?.id, activeRequisition?.currentStage?.code]);

  useEffect(() => {
    if (!activeRequisition || currentStep(activeRequisition) < 10) {
      setCommitment(null);
      setCommitmentError("");
      setCommitmentLoading(false);
      return;
    }

    let cancelled = false;
    setCommitment(null);
    setCommitmentError("");
    setCommitmentLoading(true);

    biddingApi.getCommitmentByRequisition(activeRequisition.id)
      .then((response) => {
        if (!cancelled) setCommitment(response);
      })
      .catch((requestError) => {
        if (cancelled || requestError.status === 404) return;
        setCommitmentError(requestError.message || "Não foi possível carregar o empenho.");
      })
      .finally(() => {
        if (!cancelled) setCommitmentLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [activeRequisition?.id, activeRequisition?.currentStage?.code]);

  useEffect(() => {
    if (!activeRequisition || currentStep(activeRequisition) < 11) {
      setPaymentDeclaration(null);
      setPaymentDeclarationError("");
      setPaymentDeclarationLoading(false);
      return;
    }

    let cancelled = false;
    setPaymentDeclaration(null);
    setPaymentDeclarationError("");
    setPaymentDeclarationLoading(true);

    biddingApi.getPaymentDeclarationByRequisition(activeRequisition.id)
      .then((response) => {
        if (!cancelled) setPaymentDeclaration(response);
      })
      .catch((requestError) => {
        if (cancelled || requestError.status === 404) return;
        setPaymentDeclarationError(requestError.message || "Não foi possível carregar a declaração para pagamento.");
      })
      .finally(() => {
        if (!cancelled) setPaymentDeclarationLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [activeRequisition?.id, activeRequisition?.currentStage?.code]);

  useEffect(() => {
    if (!activeRequisition || currentStep(activeRequisition) < 12) {
      setPayment(null);
      setPaymentError("");
      setPaymentLoading(false);
      return;
    }

    let cancelled = false;
    setPayment(null);
    setPaymentError("");
    setPaymentLoading(true);

    biddingApi.getPaymentByRequisition(activeRequisition.id)
      .then((response) => {
        if (!cancelled) setPayment(response);
      })
      .catch((requestError) => {
        if (cancelled || requestError.status === 404) return;
        setPaymentError(requestError.message || "Não foi possível carregar a execução do pagamento.");
      })
      .finally(() => {
        if (!cancelled) setPaymentLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [activeRequisition?.id, activeRequisition?.currentStage?.code]);

  useEffect(() => {
    if (!activeRequisition || currentStep(activeRequisition) < 13) {
      setAccountabilityReport(null);
      setAccountabilityError("");
      return;
    }

    let cancelled = false;
    setAccountabilityReport(null);
    setAccountabilityError("");

    biddingApi.getAccountabilityReportByRequisition(activeRequisition.id)
      .then((response) => {
        if (!cancelled) setAccountabilityReport(response);
      })
      .catch((requestError) => {
        if (cancelled || requestError.status === 404) return;
        setAccountabilityError(requestError.message || "Não foi possível carregar a prestação de contas.");
      });

    return () => {
      cancelled = true;
    };
  }, [activeRequisition?.id, activeRequisition?.currentStage?.code]);

  useEffect(() => {
    if (!activeRequisition || activeRequisition.currentStage?.code !== "PRESTACAO_CONTAS") {
      setAccountabilityEmployees([]);
      setAccountabilityEmployeesLoading(false);
      return;
    }

    let cancelled = false;
    setAccountabilityEmployees([]);
    setAccountabilityEmployeesLoading(true);

    biddingApi.getAccountabilityEmployees(activeRequisition.id)
      .then((response) => {
        if (!cancelled) setAccountabilityEmployees(Array.isArray(response) ? response : []);
      })
      .catch((requestError) => {
        if (!cancelled) {
          setAccountabilityError(requestError.message || "Não foi possível carregar os responsáveis pela prestação de contas.");
        }
      })
      .finally(() => {
        if (!cancelled) setAccountabilityEmployeesLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [activeRequisition?.id, activeRequisition?.currentStage?.code]);

  useEffect(() => {
    if (!licitationProcess?.id) {
      setLicitationHistory([]);
      setLicitationHistoryLoading(false);
      return;
    }

    let cancelled = false;
    setLicitationHistoryLoading(true);

    biddingApi.getLicitationHistory(licitationProcess.id)
      .then((response) => {
        if (!cancelled) setLicitationHistory(Array.isArray(response) ? response : []);
      })
      .catch(() => {
        if (!cancelled) setLicitationHistory([]);
      })
      .finally(() => {
        if (!cancelled) setLicitationHistoryLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [licitationProcess?.id, licitationHistoryRefreshKey]);

  useEffect(() => {
    if (!activeRequisition || !["RECEBIMENTO_COMPRAS", "COMPOSICAO_PROCESSO"].includes(activeRequisition.currentStage?.code)) {
      setProcurementEmployees([]);
      setProcurementAssignmentError("");
      setProcurementEmployeesLoading(false);
      return;
    }

    let cancelled = false;
    setProcurementEmployees([]);
    setProcurementAssignmentError("");
    setProcurementEmployeesLoading(true);

    biddingApi.getProcurementEmployees(activeRequisition.id)
      .then((response) => {
        if (!cancelled) setProcurementEmployees(Array.isArray(response) ? response : []);
      })
      .catch((requestError) => {
        if (!cancelled) {
          setProcurementAssignmentError(requestError.message || "Não foi possível carregar os servidores de Compras.");
        }
      })
      .finally(() => {
        if (!cancelled) setProcurementEmployeesLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [activeRequisition?.id, activeRequisition?.currentStage?.code]);

  useEffect(() => {
    if (!activeRequisition || currentStep(activeRequisition) < 6) {
      setLicitationProcess(null);
      setLicitationError("");
      setLicitationLoading(false);
      return;
    }

    let cancelled = false;
    setLicitationProcess(null);
    setLicitationError("");
    setLicitationLoading(true);

    biddingApi.getLicitationProcessByRequisition(activeRequisition.id)
      .then((response) => {
        if (!cancelled) setLicitationProcess(response);
      })
      .catch((requestError) => {
        if (cancelled || requestError.status === 404) return;
        setLicitationError(requestError.message || "Não foi possível carregar o processo licitatório.");
      })
      .finally(() => {
        if (!cancelled) setLicitationLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [activeRequisition?.id, activeRequisition?.currentStage?.code]);

  useEffect(() => {
    const stageCode = activeRequisition?.currentStage?.code;
    const isAnalysis = analysisStages.has(stageCode);
    const isApproval = approvalStages.has(stageCode);

    if (!activeRequisition || (!isAnalysis && !isApproval)) {
      setStageAction(null);
      setActionError("");
      setActionLoading(false);
      return;
    }

    let cancelled = false;
    setStageAction(null);
    setActionError("");
    setActionLoading(true);

    const request = isAnalysis
      ? biddingApi.getPendingAnalysis(activeRequisition.id)
      : biddingApi.getPendingApproval(activeRequisition.id);

    request
      .then((response) => {
        if (!cancelled) setStageAction(response);
      })
      .catch((requestError) => {
        if (cancelled) return;
        if (requestError.status === 404) {
          setStageAction(null);
          return;
        }
        setActionError(requestError.message || "Não foi possível carregar a ação pendente.");
      })
      .finally(() => {
        if (!cancelled) setActionLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [activeRequisition?.id, activeRequisition?.currentStage?.code]);

  const visibleRequisitions = useMemo(() => {
    const query = deferredSearch.trim().toLocaleLowerCase("pt-BR");
    if (!query) return requisitions;

    return requisitions.filter((item) => (
      [
        item.registerNumber,
        item.sector?.name,
        item.responsible?.name,
        acquisitionTypeLabel(item.type),
        item.technicalDescription,
        item.currentStage?.description,
      ]
        .filter(Boolean)
        .join(" ")
        .toLocaleLowerCase("pt-BR")
        .includes(query)
    ));
  }, [deferredSearch, requisitions]);

  async function createRequisition(payload) {
    setSaving(true);
    try {
      const created = await biddingApi.createRequisition(payload);
      setModalOpen(false);
      if (page !== 0) {
        setPage(0);
      } else {
        setRefreshKey((current) => current + 1);
      }
      setActiveRequisitionId(created?.id || null);
    } finally {
      setSaving(false);
    }
  }

  async function openRequisitionModal() {
    setError("");

    try {
      const response = await api.getSectors();
      setSectors(pageItems(response));
      setModalOpen(true);
    } catch (requestError) {
      setError(requestError.message || "Não foi possível carregar os setores para a nova requisição.");
    }
  }

  async function processStageAction(result, observation) {
    if (!stageAction || !activeRequisition) return;

    const isAnalysis = analysisStages.has(activeRequisition.currentStage?.code);
    setActionProcessing(result);
    setActionError("");

    try {
      if (isAnalysis) {
        await biddingApi.processAnalysis(stageAction.id, { result, observation });
      } else {
        await biddingApi.processApproval(stageAction.id, { status: result, observation });
      }

      setStageAction(null);
      setRefreshKey((current) => current + 1);
    } catch (requestError) {
      setActionError(requestError.message || "Não foi possível processar esta etapa.");
    } finally {
      setActionProcessing("");
    }
  }

  async function assignProcurementResponsible(employeeId) {
    if (!activeRequisition) return;

    setProcurementAssignmentLoading(true);
    setProcurementAssignmentError("");

    try {
      await biddingApi.assignProcurementResponsible(activeRequisition.id, employeeId);
      setRefreshKey((current) => current + 1);
    } catch (requestError) {
      setProcurementAssignmentError(requestError.message || "Não foi possível atribuir o responsável de Compras.");
    } finally {
      setProcurementAssignmentLoading(false);
    }
  }

  async function createLicitationProcess(payload) {
    setLicitationSaving(true);
    setLicitationError("");

    try {
      const created = await biddingApi.createLicitationProcess(payload);
      setLicitationProcess(created);
      setRefreshKey((current) => current + 1);
    } catch (requestError) {
      setLicitationError(requestError.message || "Não foi possível criar o processo licitatório.");
    } finally {
      setLicitationSaving(false);
    }
  }

  async function publishLicitationResult(status, observation, winnerSupplier) {
    if (!licitationProcess) return;

    setLicitationResultProcessing(status);
    setLicitationError("");

    try {
      const updated = await biddingApi.publishLicitationResult(licitationProcess.id, {
        status,
        observation,
        winnerSupplier,
      });
      setLicitationProcess(updated);
      setLicitationHistoryRefreshKey((current) => current + 1);
      if (status === "FINISHED") {
        setRefreshKey((current) => current + 1);
      }
    } catch (requestError) {
      setLicitationError(requestError.message || "Não foi possível divulgar o resultado da licitação.");
    } finally {
      setLicitationResultProcessing("");
    }
  }

  async function advanceContractStage() {
    if (!activeRequisition) return;

    setContractStageAdvancing(true);
    setContractStageError("");

    try {
      await biddingApi.advanceRequisitionStage(activeRequisition.id, {
        nextStage: "INICIO_SERVICOS",
        observation: "Processo encaminhado pelo Setor de Contratos para início dos serviços",
      });
      setRefreshKey((current) => current + 1);
    } catch (requestError) {
      setContractStageError(requestError.message || "Não foi possível avançar para o início dos serviços.");
    } finally {
      setContractStageAdvancing(false);
    }
  }

  async function createExecutionOrder(payload) {
    if (!activeRequisition) return;

    setExecutionOrderSaving(true);
    setExecutionOrderError("");

    try {
      const created = await biddingApi.createExecutionOrderForRequisition(activeRequisition.id, payload);
      setExecutionOrder(created);
      setRefreshKey((current) => current + 1);
    } catch (requestError) {
      setExecutionOrderError(requestError.message || "Não foi possível emitir a ordem de execução.");
    } finally {
      setExecutionOrderSaving(false);
    }
  }

  async function createCommitment(payload) {
    if (!activeRequisition) return;

    setCommitmentSaving(true);
    setCommitmentError("");

    try {
      const created = await biddingApi.createCommitmentForRequisition(activeRequisition.id, payload);
      setCommitment(created);
      setRefreshKey((current) => current + 1);
    } catch (requestError) {
      setCommitmentError(requestError.message || "Não foi possível emitir o empenho.");
    } finally {
      setCommitmentSaving(false);
    }
  }

  async function createPaymentDeclaration(payload) {
    if (!activeRequisition) return;

    setPaymentDeclarationSaving(true);
    setPaymentDeclarationError("");

    try {
      const created = await biddingApi.createPaymentDeclarationForRequisition(activeRequisition.id, payload);
      setPaymentDeclaration(created);
    } catch (requestError) {
      setPaymentDeclarationError(requestError.message || "Não foi possível emitir a declaração para pagamento.");
    } finally {
      setPaymentDeclarationSaving(false);
    }
  }

  async function createPayment(payload) {
    if (!activeRequisition) return;

    setPaymentSaving(true);
    setPaymentError("");

    try {
      const created = await biddingApi.createPaymentForRequisition(activeRequisition.id, payload);
      setPayment(created);
      setRefreshKey((current) => current + 1);
    } catch (requestError) {
      setPaymentError(requestError.message || "Não foi possível executar o pagamento.");
    } finally {
      setPaymentSaving(false);
    }
  }

  async function assignAccountabilityResponsible(employeeId) {
    if (!activeRequisition) return;

    setAccountabilityAssignmentLoading(true);
    setAccountabilityError("");

    try {
      const created = await biddingApi.assignAccountabilityResponsible(activeRequisition.id, employeeId);
      setAccountabilityReport(created);
      setRefreshKey((current) => current + 1);
    } catch (requestError) {
      setAccountabilityError(requestError.message || "Não foi possível atribuir o responsável pela prestação de contas.");
    } finally {
      setAccountabilityAssignmentLoading(false);
    }
  }

  const firstItem = pagination.totalElements === 0 ? 0 : page * PAGE_SIZE + 1;
  const lastItem = Math.min((page + 1) * PAGE_SIZE, pagination.totalElements);

  return (
    <DashboardLayout styles={["/css/processos.css"]}>
      <div className="dashboard">
        <div className="container">
          <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
            <div>
              <p className="section-label mb-0">Licitações</p>
              <h3 className="mb-0" style={{ fontSize: "1.4rem", color: "var(--azul-escuro)" }}>
                Requisições de Processo
              </h3>
            </div>
            <button className="btn-primary d-flex align-items-center gap-2" type="button" onClick={openRequisitionModal}>
              <i className="bi bi-plus-circle"></i> Nova Requisição
            </button>
          </div>

          {error && <div className="alert alert-danger">{error}</div>}

          <div className="row g-3">
            <div className="col-12 col-lg-8 d-flex flex-column gap-3">
              {loading ? (
                <div className="processo-header text-center py-5 text-muted">Carregando requisições...</div>
              ) : (
                <ProcessDetails
                  requisition={activeRequisition}
                  history={history}
                  historyLoading={historyLoading}
                  activeStage={activeStage}
                  stageAction={stageAction}
                  actionLoading={actionLoading}
                  actionProcessing={actionProcessing}
                  actionError={actionError}
                  canProcessPurchaseStage={
                    activeRequisition?.currentStage?.code === "PROCESSO_LICITATORIO"
                      ? canManageLicitationProcess
                      : canProcessPurchaseStage
                  }
                  procurementEmployees={procurementEmployees}
                  procurementEmployeesLoading={procurementEmployeesLoading}
                  procurementAssignmentLoading={procurementAssignmentLoading}
                  procurementAssignmentError={procurementAssignmentError}
                  licitationProcess={licitationProcess}
                  licitationHistory={licitationHistory}
                  licitationHistoryLoading={licitationHistoryLoading}
                  licitationLoading={licitationLoading}
                  licitationSaving={licitationSaving}
                  licitationResultProcessing={licitationResultProcessing}
                  licitationError={licitationError}
                  canApproveContractStage={canApproveContractStage}
                  contractStageAdvancing={contractStageAdvancing}
                  contractStageError={contractStageError}
                  executionOrder={executionOrder}
                  executionOrderLoading={executionOrderLoading}
                  executionOrderSaving={executionOrderSaving}
                  executionOrderError={executionOrderError}
                  canIssueExecutionOrder={canIssueExecutionOrder}
                  commitment={commitment}
                  commitmentLoading={commitmentLoading}
                  commitmentSaving={commitmentSaving}
                  commitmentError={commitmentError}
                  canIssueCommitment={canIssueCommitment}
                  paymentDeclaration={paymentDeclaration}
                  paymentDeclarationLoading={paymentDeclarationLoading}
                  paymentDeclarationSaving={paymentDeclarationSaving}
                  paymentDeclarationError={paymentDeclarationError}
                  canIssuePaymentDeclaration={canIssuePaymentDeclaration}
                  payment={payment}
                  paymentLoading={paymentLoading}
                  paymentSaving={paymentSaving}
                  paymentError={paymentError}
                  canExecutePayment={canExecutePayment}
                  accountabilityReport={accountabilityReport}
                  accountabilityEmployees={accountabilityEmployees}
                  accountabilityEmployeesLoading={accountabilityEmployeesLoading}
                  accountabilityAssignmentLoading={accountabilityAssignmentLoading}
                  accountabilityError={accountabilityError}
                  canProcessAccountability={canProcessAccountability}
                  onProcessAction={processStageAction}
                  onAssignProcurementResponsible={assignProcurementResponsible}
                  onCreateLicitationProcess={createLicitationProcess}
                  onPublishLicitationResult={publishLicitationResult}
                  onAdvanceContractStage={advanceContractStage}
                  onCreateExecutionOrder={createExecutionOrder}
                  onCreateCommitment={createCommitment}
                  onCreatePaymentDeclaration={createPaymentDeclaration}
                  onCreatePayment={createPayment}
                  onAssignAccountabilityResponsible={assignAccountabilityResponsible}
                  onStageChange={(code) => {
                    setActiveStage(code);
                    document.getElementById("stepperContent")?.scrollTo({ top: 0 });
                  }}
                />
              )}
            </div>

            <div className="col-12 col-lg-4 painel-lista delay-2">
              <div className="filtros-wrap">
                <h4 className="mb-0" style={{ fontSize: "0.9rem" }}>
                  <i className="bi bi-grid-fill primary me-1"></i> Requisições
                </h4>
                <div className="lista-busca-wrap">
                  <i className="bi bi-search"></i>
                  <input
                    type="search"
                    className="lista-busca"
                    placeholder="Buscar nesta página..."
                    value={listSearch}
                    onChange={(event) => setListSearch(event.target.value)}
                  />
                </div>
              </div>

              <div className="lista-processos">
                {!loading && visibleRequisitions.map((requisition) => (
                  <button
                    className={`processo-item ${activeRequisitionId === requisition.id ? "active" : ""}`}
                    type="button"
                    onClick={() => setActiveRequisitionId(requisition.id)}
                    key={requisition.id}
                  >
                    <div className="processo-item-titulo">{requisition.registerNumber || "Requisição sem número"}</div>
                    <div className="d-flex align-items-center justify-content-between gap-1">
                      <span className="processo-item-orgao">{requisition.sector?.name || "Setor não informado"}</span>
                      <span className="processo-item-status andamento">
                        Etapa {currentStep(requisition)}
                      </span>
                    </div>
                  </button>
                ))}

                {!loading && visibleRequisitions.length === 0 && (
                  <div className="p-4 text-center text-muted small">Nenhuma requisição encontrada.</div>
                )}

                <div className="lista-paginacao">
                  <span className="pag-info">{firstItem}-{lastItem} de {pagination.totalElements}</span>
                  <div className="pag-btns">
                    <button className="pag-btn" disabled={page === 0 || loading} type="button" onClick={() => setPage(0)}>
                      <i className="bi bi-chevron-double-left"></i>
                    </button>
                    <button className="pag-btn" disabled={page === 0 || loading} type="button" onClick={() => setPage((current) => current - 1)}>
                      <i className="bi bi-chevron-left"></i>
                    </button>
                    <button className="pag-btn" disabled={pagination.last || loading} type="button" onClick={() => setPage((current) => current + 1)}>
                      <i className="bi bi-chevron-right"></i>
                    </button>
                    <button
                      className="pag-btn"
                      disabled={pagination.last || loading}
                      type="button"
                      onClick={() => setPage(Math.max(pagination.totalPages - 1, 0))}
                    >
                      <i className="bi bi-chevron-double-right"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <NewProcessModal
        open={modalOpen}
        sectors={sectors}
        saving={saving}
        onClose={() => setModalOpen(false)}
        onCreated={createRequisition}
      />
    </DashboardLayout>
  );
}
