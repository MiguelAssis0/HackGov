import { useEffect, useState, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link } from "../components/RouterContext.jsx";
import { api, getStoredUser, getSelectedCityHall } from "../services/api.js";

const PAGE_SIZE = 8;
const typeLabels = { TASK: "Tarefa", DOCUMENT: "Documento", ALERT: "Alerta", REQUEST: "Solicitacao" };
const statusLabels = { NEW: "Nova", IN_PROGRESS: "Em andamento", COMPLETED: "Concluida", ARCHIVED: "Arquivada" };
const priorityLabels = { LOW: "Baixa", NORMAL: "Normal", HIGH: "Alta" };

function formatDate(value) {
  if (!value) return "—";
  try {
    const d = new Date(value);
    return d.toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" });
  } catch { return String(value).slice(0, 16); }
}

export default function InboxPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const caixa = searchParams.get("caixa") === "setor" ? "setor" : "pessoal";
  const leitura = ["nao-lidas", "lidas"].includes(searchParams.get("leitura")) ? searchParams.get("leitura") : "";
  const q = searchParams.get("q") || "";
  const setor = searchParams.get("setor") || "";
  const page = Number(searchParams.get("page") || "0");

  const [entries, setEntries] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [counts, setCounts] = useState({ minhasNaoLidas: 0, setorNaoLidas: 0 });
  const [setores, setSetores] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [searchInput, setSearchInput] = useState(q);

  const user = getStoredUser();
  const cityHall = getSelectedCityHall() || { name: user?.prefeitura || user?.cityHall?.name || "Prefeitura" };
  const canViewAll = ["admin_cidade", "admin_equipe"].includes(user?.tipoUsuario || user?.role);

  const updateParams = (patch) => {
    const next = new URLSearchParams(searchParams);
    Object.entries(patch).forEach(([k, v]) => {
      if (v === "" || v == null) next.delete(k);
      else next.set(k, String(v));
    });
    if ("q" in patch || "caixa" in patch || "leitura" in patch || "setor" in patch) next.delete("page");
    setSearchParams(next, { replace: true });
  };

  useEffect(() => setSearchInput(q), [q]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const payload = await api.getInbox({ page, caixa, leitura, q, setor });
      const content = payload?.content || [];
      setEntries(content);
      setTotalPages(payload?.totalPages || 0);
      setTotalElements(payload?.totalElements || 0);
      try {
        const c = await api.getInboxCounts({ setor, query: q });
        setCounts(c);
      } catch { /* ignore counts */ }
      setMessage(null);
    } catch (error) {
      setMessage({ type: "danger", text: error.message });
    } finally { setLoading(false); }
  }, [page, caixa, leitura, q, setor]);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    api.getSectorsInbox().then((data) => {
      const list = Array.isArray(data) ? data : data?.content || [];
      setSetores(list.map((s) => ({ id: s.id, nome: s.name || s.nome })));
    }).catch(() => {});
  }, []);

  // debounce search
  useEffect(() => {
    if (searchInput === q) return;
    const t = setTimeout(() => updateParams({ q: searchInput.trim() }), searchInput ? 300 : 0);
    return () => clearTimeout(t);
  }, [searchInput]); // eslint-disable-line

  const qsWithoutPage = () => {
    const p = new URLSearchParams(searchParams);
    p.delete("page");
    return p.toString();
  };

  return (
    <DashboardLayout styles={["/css/caixa-entrada.css"]}>
      <main className="dashboard">
        <div className="container inbox-page">
          <div className="inbox-header-card">
            <div className="d-flex align-items-center justify-content-between flex-wrap gap-3">
              <div>
                <p className="text-white-50 small text-uppercase fw-bold mb-1">{cityHall.name}</p>
                <h2 className="h3 fw-bold mb-0 text-white"><i className="bi bi-inbox-fill me-2"></i>Caixa de Entrada</h2>
                <p className="text-white-50 mb-0 small mt-1">Acompanhe mensagens pessoais e demandas recebidas pelo setor.</p>
              </div>
            </div>
          </div>

          <div className="row mb-4 align-items-center">
            <div className="col-12">
              <div className="inbox-toolbar">
                <div className="inbox-search-bar">
                  <i className="bi bi-search"></i>
                  <input type="search" value={searchInput} onChange={(e) => setSearchInput(e.target.value)} placeholder="Pesquisar por assunto, remetente ou conteúdo..." />
                  {q && <button className="btn btn-sm btn-link text-secondary" onClick={() => updateParams({ q: "" })} aria-label="Limpar pesquisa"><i className="bi bi-x-circle-fill"></i></button>}
                </div>
                <label className="inbox-sector-filter">
                  <i className="bi bi-building"></i>
                  <select value={setor} onChange={(e) => updateParams({ setor: e.target.value })} disabled={!canViewAll}>
                    <option value="">Todos os setores</option>
                    {setores.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
                  </select>
                </label>
              </div>
            </div>
          </div>

          <div className="docs-tabs inbox-tabs" role="tablist">
            <button className={`docs-tab-btn ${caixa === "pessoal" ? "active" : ""}`} onClick={() => updateParams({ caixa: "pessoal" })}>
              <i className="bi bi-person-fill-lock me-1"></i> Minha Caixa
              {counts.minhasNaoLidas > 0 && <span className="inbox-tab-badge">{counts.minhasNaoLidas}</span>}
            </button>
            <button className={`docs-tab-btn ${caixa === "setor" ? "active" : ""}`} onClick={() => updateParams({ caixa: "setor" })}>
              <i className="bi bi-building-fill me-1"></i> Caixa do Setor
              {counts.setorNaoLidas > 0 && <span className="inbox-tab-badge">{counts.setorNaoLidas}</span>}
            </button>
          </div>

          <div className="inbox-mailbox">
            <div className="inbox-mailbox-head">
              <div>
                <p className="eyebrow dark mb-0">{caixa === "pessoal" ? "Minha Caixa" : canViewAll ? "Caixas dos Setores" : (user?.setor || "Meu Setor")}</p>
                <h4>{leitura === "nao-lidas" ? "Não Lidas" : leitura === "lidas" ? "Lidas" : "Todas as mensagens"}</h4>
              </div>
              <nav className="inbox-read-tabs" aria-label="Filtro de leitura">
                <button className={!leitura ? "active" : ""} onClick={() => updateParams({ leitura: "" })}><i className="bi bi-inboxes"></i><span>Todas</span></button>
                <button className={leitura === "nao-lidas" ? "active" : ""} onClick={() => updateParams({ leitura: "nao-lidas" })}>
                  <i className="bi bi-envelope"></i><span>Não Lidas</span>
                  {(caixa === "pessoal" ? counts.minhasNaoLidas : counts.setorNaoLidas) > 0 && <strong>{caixa === "pessoal" ? counts.minhasNaoLidas : counts.setorNaoLidas}</strong>}
                </button>
                <button className={leitura === "lidas" ? "active" : ""} onClick={() => updateParams({ leitura: "lidas" })}><i className="bi bi-envelope-open"></i><span>Lidas</span></button>
              </nav>
            </div>

            <div className="inbox-message-list">
              {entries.length > 0 && (
                <div className="inbox-message-columns" aria-hidden="true">
                  <span>Assunto</span><span>Remetente</span><span>Destinatário</span><span>Prioridade</span><span>Data e Hora</span>
                </div>
              )}
              {entries.map((entrada) => (
                <Link
                  key={entrada.id}
                  to={`/caixa-entrada/${entrada.id}?next=${encodeURIComponent(`/caixa-entrada?${qsWithoutPage()}`)}`}
                  className={`inbox-message-item ${!entrada.readAt ? "unread" : ""}`}
                >
                  <span className="inbox-message-body">
                    <span className="inbox-unread-dot" aria-hidden="true"></span>
                    <span className="inbox-message-copy">
                      <strong>{entrada.title}</strong>
                      <small>{entrada.description ? entrada.description.slice(0, 120) : "Sem conteúdo."}</small>
                    </span>
                  </span>
                  <span className="inbox-message-sender">{entrada.senderName || "Sistema"}</span>
                  <span className="inbox-message-destination">{entrada.destinationEmployeeName || entrada.destinationSectorName || "Prefeitura"}</span>
                  <span className={`inbox-message-priority priority-${String(entrada.priority || "NORMAL").toLowerCase()}`}>{priorityLabels[entrada.priority] || entrada.priority}</span>
                  <span className="inbox-message-meta"><time dateTime={entrada.createdAt}>{formatDate(entrada.createdAt)}</time></span>
                </Link>
              ))}
              {!loading && entries.length === 0 && (
                <div className="inbox-empty"><i className="bi bi-inbox"></i><span>Nenhuma mensagem encontrada.</span></div>
              )}
              {loading && <div className="inbox-empty"><span>Carregando...</span></div>}
            </div>

            {totalPages > 1 && (
              <div className="inbox-pagination">
                <button disabled={page <= 0} onClick={() => updateParams({ page: page - 1 })}>Anterior</button>
                <span>Página {page + 1} de {totalPages} — {totalElements} itens</span>
                <button disabled={page >= totalPages - 1} onClick={() => updateParams({ page: page + 1 })}>Próxima</button>
              </div>
            )}
            {message && <div className={`auth-message ${message.type === "danger" ? "error" : "success"} m-3`}>{message.text}</div>}
          </div>
        </div>
      </main>
    </DashboardLayout>
  );
}
