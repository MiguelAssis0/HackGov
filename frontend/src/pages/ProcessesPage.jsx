import { useEffect, useMemo, useRef, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";

const sectors = [
  "Administração Geral",
  "Secretaria de Obras",
  "Secretaria de Educação",
  "Secretaria de Saúde",
  "Secretaria da Fazenda",
  "Assistência Social",
  "Secretaria de Urbanismo",
  "Secretaria de Meio Ambiente",
  "Secretaria de Cultura",
  "Secretaria de Transporte",
];

const steps = [
  { section: "Secretaria de Origem", id: "p01", label: "01. Cadastro da Requisição", sublabel: "01/04/2026", done: true },
  { id: "p02", label: "02. Homologação Secretário", sublabel: "02/04/2026", done: true },
  { section: "Setor de Compras", id: "p03", label: "03. Recebimento Compras", sublabel: "03/04/2026", done: true },
  { id: "p04", label: "04. Análise da Requisição", sublabel: "Em andamento", current: true },
  { id: "p05", label: "05. Homologação Compras", sublabel: "Aguardando" },
  { id: "p06", label: "06. Composição do Processo", sublabel: "Aguardando" },
  { id: "p07", label: "07. Processo Licitatório", sublabel: "Aguardando" },
  { id: "p08", label: "08. Setor de Contrato", sublabel: "Aguardando" },
  { section: "Secretaria de Origem", id: "p09", label: "09. Início dos Serviços", sublabel: "Aguardando" },
  { id: "p10", label: "10. Emissão de Empenho", sublabel: "Aguardando" },
  { id: "p11", label: "11. Declaração p/ Pagamento", sublabel: "Aguardando" },
  { section: "Secretaria da Fazenda", id: "p12", label: "12. Execução do Pagamento", sublabel: "Aguardando" },
  { section: "Controle / Prestação", id: "p13", label: "13. Enc. Prestação de Contas", sublabel: "Aguardando" },
  { id: "p14", label: "14. Análise Prestação Contas", sublabel: "Aguardando" },
  { id: "p15", label: "15. Homologação Prestação", sublabel: "Aguardando" },
];

const panels = {
  p01: {
    icon: "bi-check-circle-fill",
    title: "01 - Cadastro da Requisição",
    badge: ["bi-check-circle-fill", "Concluído em 01/04/2026", "#dcfce7", "#16a34a"],
    rows: [
      ["Responsável", "João da Silva - Secretaria de Obras"],
      ["Nº da Requisição", "REQ-2026-00147"],
      ["Dotação Orçamentária", "02.004.1520.44905200.00"],
      ["Justificativa", "Necessidade de manutenção emergencial de vias urbanas deterioradas pelo período de chuvas."],
    ],
    docs: [
      ["bi-file-earmark-pdf-fill", "Descrição Técnica", "PDF · 180 KB"],
      ["bi-file-earmark-word-fill", "Justificativa", "DOCX · 45 KB"],
      ["bi-file-earmark-pdf-fill", "ETP", "PDF · 512 KB"],
    ],
  },
  p02: {
    icon: "bi-check-circle-fill",
    title: "02 - Homologação pelo Secretário",
    badge: ["bi-check-circle-fill", "Aprovado em 02/04/2026", "#dcfce7", "#16a34a"],
    rows: [
      ["Aprovado por", "Carlos Mendes - Secretário de Obras"],
      ["Data de aprovação", "02/04/2026 às 10:30h"],
      ["Observação", "Requisição aprovada pelo secretário. Processo encaminhado ao setor de compras para análise."],
    ],
  },
  p03: {
    icon: "bi-check-circle-fill",
    title: "03 - Recebimento pela Área de Compras",
    badge: ["bi-check-circle-fill", "Recebido em 03/04/2026", "#dcfce7", "#16a34a"],
    rows: [
      ["Recebido por", "Maria Souza - Setor de Compras"],
      ["Data de recebimento", "03/04/2026 às 08:15h"],
      ["E-mail de confirmação", "Enviado automaticamente ao setor de origem em 03/04/2026 às 08:16h"],
    ],
  },
  p04: {
    icon: "bi-clock-fill",
    title: "04 - Análise da Requisição",
    badge: ["bi-hourglass-split", "Em análise - aguardando resultado", "#e8f2ff", "var(--azul)"],
    rows: [
      ["Analista responsável", "Maria Souza - Setor de Compras"],
      ["Início da análise", "05/04/2026 às 09:00h"],
      ["Prazo para conclusão", "15/05/2026"],
    ],
    statuses: ["Aprovada", "Necessita Correção", "Reprovada", "Cancelada"],
  },
  p05: {
    icon: "bi-patch-check",
    title: "05 - Homologação pela Área de Compras",
    badge: ["bi-hourglass", "Aguardando conclusão da etapa anterior", "var(--cinza-claro)", "var(--text-muted)"],
    text: "Após aprovação na análise, o responsável da área de compras homologa o processo e dispara e-mail de resultado ao setor de origem.",
  },
  p06: {
    icon: "bi-file-earmark-ruled",
    title: "06 - Composição do Processo",
    badge: ["bi-hourglass", "Aguardando etapas anteriores", "var(--cinza-claro)", "var(--text-muted)"],
    rows: [
      ["Base legal", "Lei nº 14.133/2021"],
      [
        "Modalidades previstas",
        "Chamada Pública, Concorrência, Pregão Eletrônico, Pregão Presencial, Tomada de Preços, Extrato de Fomento, Convite, Outros",
      ],
      ["Modalidade provável", "Pregão Eletrônico (valor estimado: R$ 48.500,00)"],
    ],
  },
  p07: {
    icon: "bi-gavel",
    title: "07 - Processo Licitatório",
    badge: ["bi-hourglass", "Aguardando etapas anteriores", "var(--cinza-claro)", "var(--text-muted)"],
    rows: [["Fases", "Publicação do Edital → Realização da Licitação → Divulgação do Resultado"]],
    statuses: ["Andamento", "Finalizado", "Impugnado", "Postergado", "Encerrado"],
  },
  p08: {
    icon: "bi-file-earmark-text",
    title: "08 - Setor de Contrato",
    badge: ["bi-hourglass", "Aguardando etapas anteriores", "var(--cinza-claro)", "var(--text-muted)"],
    text: "Após definição da empresa vencedora, o setor de contratos realiza o contato, encaminhamento e assinatura do contrato administrativo entre as partes. Ao concluir, o setor de origem é notificado por e-mail.",
  },
  p09: {
    icon: "bi-play-circle",
    title: "09 - Início dos Serviços",
    badge: ["bi-hourglass", "Aguardando etapas anteriores", "var(--cinza-claro)", "var(--text-muted)"],
    rows: [
      ["Para aquisição", "Emissão de Ordem de Fornecimento"],
      ["Para serviço", "Emissão de Ordem de Serviço"],
      ["Encaminhamento", "Pelo servidor competente do setor de origem"],
    ],
  },
  p10: {
    icon: "bi-receipt",
    title: "10 - Emissão de Empenho",
    badge: ["bi-hourglass", "Aguardando etapas anteriores", "var(--cinza-claro)", "var(--text-muted)"],
    rows: [
      ["Bens tombados", "Empenho emitido pelo Patrimônio"],
      ["Expediente / consumo", "Empenho emitido pela secretaria de origem"],
      ["Serviços", "Empenho para pagamento emitido pela secretaria de origem"],
    ],
  },
  p11: {
    icon: "bi-file-earmark-check",
    title: "11 - Emissão da Declaração para Pagamento",
    badge: ["bi-hourglass", "Aguardando etapas anteriores", "var(--cinza-claro)", "var(--text-muted)"],
    rows: [
      ["Para itens adquiridos", "Declaração de recebimento dos itens"],
      ["Para serviços", "Medição dos serviços prestados"],
      ["Aprovação", "Pelo Secretário da Pasta"],
    ],
  },
  p12: {
    icon: "bi-bank",
    title: "12 - Execução do Pagamento",
    badge: ["bi-hourglass", "Aguardando etapas anteriores", "var(--cinza-claro)", "var(--text-muted)"],
    rows: [
      ["Executor", "Secretaria da Fazenda"],
      ["Modalidades", "Pagamento direto / Medição para serviços"],
      ["Aprovação", "Pelo Secretário da Fazenda"],
    ],
  },
  p13: {
    icon: "bi-send",
    title: "13 - Encaminhamento da Prestação de Contas",
    badge: ["bi-hourglass", "Aguardando etapas anteriores", "var(--cinza-claro)", "var(--text-muted)"],
    text: "Documentação completa encaminhada à área de controle para análise da prestação de contas.",
  },
  p14: {
    icon: "bi-search",
    title: "14 - Análise da Prestação de Contas",
    badge: ["bi-hourglass", "Aguardando etapas anteriores", "var(--cinza-claro)", "var(--text-muted)"],
    statuses: ["Em Análise", "Pendente", "Em Diligência", "Reprovada", "Aprovada"],
  },
  p15: {
    icon: "bi-trophy",
    title: "15 - Homologação da Prestação de Contas",
    badge: ["bi-hourglass", "Aguardando etapas anteriores", "var(--cinza-claro)", "var(--text-muted)"],
    text: "Etapa final do processo. Quando encerrada ou aprovada, o responsável da área homologa e dispara e-mail ao setor de origem confirmando a conclusão.",
    rows: [["Aprovação", "Pelo responsável da Área de Controle"]],
  },
};

const processItems = [
  ["Processo 001/2026 - Obras", "Secretaria de Obras", "andamento", "Andamento"],
  ["Processo 002/2026 - Educação", "Secretaria de Educação", "aberto", "Aberto"],
  ["Processo 003/2026 - Saúde", "Secretaria de Saúde", "andamento", "Andamento"],
  ["Processo 004/2026 - Fazenda", "Secretaria da Fazenda", "encerrado", "Encerrado"],
  ["Processo 005/2026 - Administração", "Administração Geral", "suspenso", "Suspenso"],
  ["Processo 006/2026 - Assistência", "Assistência Social", "aberto", "Aberto"],
  ["Processo 007/2026 - Meio Ambiente", "Sec. de Meio Ambiente", "encerrado", "Encerrado"],
  ["Processo 008/2026 - Urbanismo", "Secretaria de Urbanismo", "andamento", "Andamento"],
  ["Processo 009/2026 - Cultura", "Secretaria de Cultura", "aberto", "Aberto"],
  ["Processo 010/2026 - Transporte", "Sec. de Transporte", "suspenso", "Suspenso"],
];

function statusClass(label) {
  if (["Aprovada", "Finalizado"].includes(label)) return "status-aberto";
  if (["Reprovada", "Impugnado", "Encerrado"].includes(label)) return "status-encerrado";
  if (["Pendente", "Em Diligência", "Cancelada", "Postergado"].includes(label)) return "status-suspenso";
  return "status-andamento";
}

function StepPanel({ id, active }) {
  const panel = panels[id];
  const [badgeIcon, badgeText, badgeBg, badgeColor] = panel.badge;

  return (
    <div className={`step-panel ${active ? "active" : ""}`} id={id}>
      <div className="secao-titulo">
        <i className={`bi ${panel.icon}`}></i> {panel.title}
      </div>
      <div className="d-inline-flex align-items-center gap-2 px-3 py-2 rounded-3 mb-3" style={{ background: badgeBg }}>
        <i className={`bi ${badgeIcon}`} style={{ color: badgeColor }}></i>
        <span style={{ fontSize: "0.82rem", fontWeight: 600, color: badgeColor }}>{badgeText}</span>
      </div>

      {panel.text && <p style={{ fontSize: "0.85rem", color: "var(--text-muted)" }}>{panel.text}</p>}

      {panel.rows?.map(([key, value]) => (
        <div className="detalhe-row" key={key}>
          <div className="detalhe-chave">{key}</div>
          <div className="detalhe-valor">{value}</div>
        </div>
      ))}

      {panel.statuses && (
        <div className="detalhe-row">
          <div className="detalhe-chave">{id === "p04" ? "Possíveis resultados" : "Status possíveis"}</div>
          <div className="detalhe-valor">
            <div className="d-flex flex-wrap gap-1 mt-1">
              {panel.statuses.map((status) => (
                <span className={`status-badge ${statusClass(status)}`} key={status}>
                  {status}
                </span>
              ))}
            </div>
          </div>
        </div>
      )}

      {panel.docs && (
        <div className="mt-3">
          <div className="secao-titulo">
            <i className="bi bi-paperclip"></i> Documentos da Requisição
          </div>
          <div className="docs-grid">
            {panel.docs.map(([icon, name, size]) => (
              <a href="#" className="doc-item" key={name}>
                <div className="doc-icon">
                  <i className={`bi ${icon}`}></i>
                </div>
                <span className="doc-nome">{name}</span>
                <span className="doc-tamanho">{size}</span>
              </a>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function NewProcessModal({ open, onClose }) {
  const fileInputRef = useRef(null);
  const [files, setFiles] = useState([]);

  useEffect(() => {
    document.body.classList.toggle("modal-open", open);
    return () => document.body.classList.remove("modal-open");
  }, [open]);

  if (!open) return null;

  function submit(event) {
    event.preventDefault();
    onClose();
  }

  return (
    <div className="react-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalNovoProcessoLabel">
      <div className="react-modal-card">
        <div className="modal-header" style={{ borderBottom: "var(--border1)", padding: "1.25rem 1.5rem" }}>
          <div>
            <h5 className="modal-title mb-0" id="modalNovoProcessoLabel" style={{ fontWeight: 700, color: "var(--azul-escuro)" }}>
              <i className="bi bi-plus-circle primary me-2"></i>Nova Requisição de Processo
            </h5>
            <small style={{ color: "var(--text-muted)", fontSize: "0.78rem" }}>Etapa 01 - Cadastro da Requisição</small>
          </div>
          <button type="button" className="btn-close" aria-label="Fechar" onClick={onClose}></button>
        </div>

        <div className="modal-body" style={{ padding: "1.5rem" }}>
          <form id="formNovoProcesso" onSubmit={submit}>
            <div className="mb-3">
              <label className="form-label" style={modalLabelStyle}>
                Objeto / Título da Requisição
              </label>
              <input type="text" className="form-control" name="titulo" placeholder="Ex: Aquisição de materiais de construção civil" required style={modalInputStyle} />
            </div>

            <div className="row g-3 mb-3">
              <div className="col-12 col-sm-6">
                <label className="form-label" style={modalLabelStyle}>
                  Secretaria / Setor de Origem
                </label>
                <select className="form-select" name="setor" required style={modalInputStyle}>
                  <option value="">Selecionar...</option>
                  {sectors.map((sector) => (
                    <option key={sector}>{sector}</option>
                  ))}
                </select>
              </div>
              <div className="col-12 col-sm-6">
                <label className="form-label" style={modalLabelStyle}>
                  Tipo de Aquisição
                </label>
                <select className="form-select" name="tipo" required style={modalInputStyle}>
                  <option value="">Selecionar...</option>
                  <option>Bem Móvel</option>
                  <option>Bem Imóvel</option>
                  <option>Serviço (PJ)</option>
                  <option>Emenda Impositiva</option>
                </select>
              </div>
            </div>

            <div className="row g-3 mb-3">
              <div className="col-12 col-sm-6">
                <label className="form-label" style={modalLabelStyle}>
                  Servidor Requisitante
                </label>
                <input type="text" className="form-control" name="requisitante" placeholder="Nome do servidor" required style={modalInputStyle} />
              </div>
              <div className="col-12 col-sm-6">
                <label className="form-label" style={modalLabelStyle}>
                  Valor Estimado (R$)
                </label>
                <input type="text" className="form-control" name="valor" placeholder="Ex: 48.500,00" style={modalInputStyle} />
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label" style={modalLabelStyle}>
                Dotação Orçamentária
              </label>
              <input type="text" className="form-control" name="dotacao" placeholder="Ex: 02.004.1520.44905200.00" style={modalInputStyle} />
            </div>

            <div className="mb-3">
              <label className="form-label" style={modalLabelStyle}>
                Justificativa da Necessidade
              </label>
              <textarea
                className="form-control"
                name="justificativa"
                rows="3"
                placeholder="Descreva a necessidade e motivação da aquisição..."
                required
                style={{ ...modalInputStyle, resize: "none" }}
              ></textarea>
            </div>

            <div className="mb-3">
              <label className="form-label" style={modalLabelStyle}>
                Descrição Técnica do Item / Serviço
              </label>
              <textarea
                className="form-control"
                name="descricao_tecnica"
                rows="3"
                placeholder="Especificações técnicas detalhadas..."
                style={{ ...modalInputStyle, resize: "none" }}
              ></textarea>
            </div>

            <div className="mb-1">
              <label className="form-label" style={modalLabelStyle}>
                Documentos Anexos
              </label>
              <small className="d-block mb-2" style={{ fontSize: "0.75rem", color: "var(--text-muted)" }}>
                Justificativa, Descrição Técnica, ETP (Estudo Técnico Preliminar)
              </small>
              <button
                type="button"
                className="d-flex align-items-center justify-content-center gap-2 p-4 rounded-3 w-100"
                style={{ border: "2px dashed var(--border-color)", cursor: "pointer", background: "var(--cinza-claro)" }}
                onClick={() => fileInputRef.current?.click()}
              >
                <i className="bi bi-cloud-arrow-up" style={{ fontSize: "1.4rem", color: "var(--text-muted)" }}></i>
                <div>
                  <div style={{ fontSize: "0.85rem", fontWeight: 600, color: "var(--azul-escuro)" }}>Clique para anexar arquivos</div>
                  <div style={{ fontSize: "0.75rem", color: "var(--text-muted)" }}>PDF, DOCX, XLSX - máx. 10 MB cada</div>
                </div>
              </button>
              <input
                type="file"
                ref={fileInputRef}
                name="documentos"
                multiple
                accept=".pdf,.doc,.docx,.xls,.xlsx"
                className="d-none"
                onChange={(event) => setFiles(Array.from(event.target.files || []))}
              />
              <div className="mt-2 d-flex flex-wrap gap-2">
                {files.map((file) => (
                  <div
                    className="d-flex align-items-center gap-1 px-2 py-1 rounded-3"
                    style={{ background: "var(--cinza-claro)", border: "var(--border1-5)", fontSize: "0.75rem", color: "var(--azul-escuro)" }}
                    key={`${file.name}-${file.size}`}
                  >
                    <i className="bi bi-file-earmark" style={{ color: "var(--azul)" }}></i>
                    {file.name}
                  </div>
                ))}
              </div>
            </div>
          </form>
        </div>

        <div className="modal-footer" style={{ borderTop: "var(--border1)", padding: "1rem 1.5rem", gap: "0.5rem" }}>
          <button type="button" className="btn-outline-primary" onClick={onClose}>
            Cancelar
          </button>
          <button type="submit" form="formNovoProcesso" className="btn-primary d-flex align-items-center gap-2">
            <i className="bi bi-send"></i> Enviar Requisição
          </button>
        </div>
      </div>
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
  const [activeStep, setActiveStep] = useState("p04");
  const [listSearch, setListSearch] = useState("");
  const [activeProcess, setActiveProcess] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);

  const visibleProcesses = useMemo(() => {
    const query = listSearch.toLowerCase();
    return processItems
      .map((item, index) => ({ item, index }))
      .filter(({ item }) => item.join(" ").toLowerCase().includes(query));
  }, [listSearch]);

  return (
    <DashboardLayout styles={["/css/processos.css"]}>
      <div className="dashboard">
        <div className="container">
          <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
            <div>
              <p className="section-label mb-0">Processos</p>
              <h3 className="mb-0" style={{ fontSize: "1.4rem", color: "var(--azul-escuro)" }}>
                Acompanhamento
              </h3>
            </div>
            <button className="btn-primary d-flex align-items-center gap-2" type="button" onClick={() => setModalOpen(true)}>
              <i className="bi bi-plus-circle"></i> Novo Processo
            </button>
          </div>

          <div className="row g-3">
            <div className="col-12 col-lg-8 d-flex flex-column gap-3">
              <div className="processo-header">
                <div className="d-flex align-items-flex-start justify-content-between gap-2 flex-wrap">
                  <h3 className="processo-titulo">Processo nº 001/2026 - Setor de Obras</h3>
                  <div className="d-flex gap-2">
                    <a href="#" className="btn-acao" title="Copiar link">
                      <i className="bi bi-link-45deg"></i>
                    </a>
                    <a href="#" className="btn-acao" title="Imprimir">
                      <i className="bi bi-printer"></i>
                    </a>
                  </div>
                </div>

                <div className="processo-meta-grid">
                  {[
                    ["Setor Responsável", "Secretaria de Obras"],
                    ["Tipo", "Pregão Eletrônico"],
                    ["Abertura", "01/04/2026"],
                    ["Situação", <span className="status-badge status-andamento">Em Andamento</span>],
                    ["Valor Estimado", "R$ 48.500,00"],
                    ["Etapa Atual", "04 - Análise da Requisição"],
                  ].map(([label, value]) => (
                    <div key={label}>
                      <div className="meta-label">{label}</div>
                      <div className="meta-valor">{value}</div>
                    </div>
                  ))}
                  <div style={{ gridColumn: "1 / -1" }}>
                    <div className="meta-label">Objeto</div>
                    <div className="meta-valor descricao-completa">
                      Aquisição de materiais de construção civil para manutenção de vias públicas no perímetro
                      urbano, conforme especificações técnicas do Termo de Referência e demais documentos anexos
                      ao processo.
                    </div>
                  </div>
                </div>
              </div>

              <div className="processo-card delay-1">
                <div className="stepper-wrap">
                  <div className="stepper-nav">
                    {steps.map((step, index) => (
                      <div key={step.id}>
                        {step.section && <div className="step-section-label ms-3">{step.section}</div>}
                        <button
                          className={`step-btn ${activeStep === step.id ? "active" : ""} ${step.done ? "concluido-btn" : ""}`}
                          type="button"
                          onClick={() => {
                            setActiveStep(step.id);
                            document.getElementById("stepperContent").scrollTop = 0;
                          }}
                        >
                          <div className={`step-indicator ${step.done ? "concluido" : step.current ? "atual" : "pendente"}`}>
                            <i
                              className={`bi ${
                                step.done ? "bi-check" : step.current ? "bi-circle-fill" : "bi-circle"
                              }`}
                              style={{ fontSize: step.done ? undefined : step.current ? "0.35rem" : "0.45rem" }}
                            ></i>
                          </div>
                          <div>
                            <div className="step-label">{step.label}</div>
                            <div className="step-sublabel">{step.sublabel}</div>
                          </div>
                        </button>
                        {index < steps.length - 1 && (
                          <div className={`step-connector ${step.done ? "concluido" : step.current ? "atual" : ""}`}></div>
                        )}
                      </div>
                    ))}
                  </div>

                  <div className="stepper-content" id="stepperContent">
                    {steps.map((step) => (
                      <StepPanel id={step.id} active={activeStep === step.id} key={step.id} />
                    ))}
                  </div>
                </div>
              </div>
            </div>

            <div className="col-12 col-lg-4 painel-lista delay-2">
              <div className="filtros-wrap">
                <div className="d-flex align-items-center justify-content-between mb-1">
                  <h4 className="mb-0" style={{ fontSize: "0.9rem" }}>
                    <i className="bi bi-grid-fill primary me-1"></i> Processos
                  </h4>
                  <button className="btn-acao" title="Filtros avançados" type="button">
                    <i className="bi bi-funnel"></i>
                  </button>
                </div>
                <div className="lista-busca-wrap">
                  <i className="bi bi-search"></i>
                  <input
                    type="text"
                    className="lista-busca"
                    placeholder="Buscar processo..."
                    value={listSearch}
                    onChange={(event) => setListSearch(event.target.value)}
                  />
                </div>
              </div>

              <div className="lista-processos">
                {visibleProcesses.map(({ item, index }) => {
                  const [title, agency, statusClassName, statusLabel] = item;
                  return (
                    <button
                      className={`processo-item ${activeProcess === index ? "active" : ""}`}
                      type="button"
                      onClick={() => setActiveProcess(index)}
                      key={title}
                    >
                      <div className="processo-item-titulo">{title}</div>
                      <div className="d-flex align-items-center justify-content-between gap-1">
                        <span className="processo-item-orgao">{agency}</span>
                        <span className={`processo-item-status ${statusClassName}`}>{statusLabel}</span>
                      </div>
                    </button>
                  );
                })}

                <div className="lista-paginacao">
                  <span className="pag-info">1-10 de 248</span>
                  <div className="pag-btns">
                    <button className="pag-btn" disabled type="button">
                      <i className="bi bi-chevron-double-left"></i>
                    </button>
                    <button className="pag-btn" disabled type="button">
                      <i className="bi bi-chevron-left"></i>
                    </button>
                    <button className="pag-btn" type="button">
                      <i className="bi bi-chevron-right"></i>
                    </button>
                    <button className="pag-btn" type="button">
                      <i className="bi bi-chevron-double-right"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <NewProcessModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </DashboardLayout>
  );
}
