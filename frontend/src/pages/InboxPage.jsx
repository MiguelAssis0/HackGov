import { useEffect, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { PageHeader } from "../components/DashboardShared.jsx";
import { Link } from "../components/RouterContext.jsx";
import { api } from "../services/api.js";

const statusLabels = { NEW: "Nova", IN_PROGRESS: "Em andamento", COMPLETED: "Concluida", ARCHIVED: "Arquivada" };
const typeLabels = { TASK: "Tarefa", DOCUMENT: "Documento", ALERT: "Alerta", REQUEST: "Solicitacao" };

export default function InboxPage() {
  const [entries, setEntries] = useState([]);
  const [filters, setFilters] = useState({ status: "", type: "", unreadOnly: false, query: "" });
  const [selected, setSelected] = useState(null);
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    try {
      const payload = await api.getInbox(filters);
      setEntries(payload?.content || []);
      setMessage(null);
    } catch (error) {
      setMessage({ type: "danger", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(load, filters.query ? 250 : 0);
    return () => window.clearTimeout(timer);
  }, [filters]);

  async function openEntry(entry) {
    try {
      const next = entry.readAt ? entry : await api.readInboxEntry(entry.id);
      setSelected(next);
      setEntries((items) => items.map((item) => item.id === next.id ? next : item));
    } catch (error) {
      setMessage({ type: "danger", text: error.message });
    }
  }

  async function action(name) {
    if (!selected) return;
    try {
      const next = name === "claim" ? await api.claimInboxEntry(selected.id) : await api.completeInboxEntry(selected.id);
      setSelected(next);
      setEntries((items) => items.map((item) => item.id === next.id ? next : item));
    } catch (error) {
      setMessage({ type: "danger", text: error.message });
    }
  }

  async function answerRequest(accepted) {
    const feedback = window.prompt(accepted ? "Feedback ao setor solicitante (opcional):" : "Motivo da recusa:", "");
    if (feedback === null || (!accepted && !feedback.trim())) return;
    try {
      if (accepted) await api.acceptTaskRequest(selected.objectId, feedback); else await api.rejectTaskRequest(selected.objectId, feedback);
      setMessage({ type: "success", text: accepted ? "Demanda aceita e tarefa criada." : "Demanda recusada com feedback." }); setSelected(null); await load();
    } catch (error) { setMessage({ type: "danger", text: error.message }); }
  }

  return (
    <DashboardLayout styles={["/css/caixa-entrada.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader eyebrow="Fluxos direcionados ao servidor e ao setor" title="Caixa de Entrada" />
          {message && <div className={`auth-message ${message.type} mb-3`}>{message.text}</div>}
          <section className="inbox-shell">
            <aside className="inbox-filters">
              <label className="field-label">Buscar</label>
              <input className="field-input" placeholder="Remetente, assunto ou conteudo" value={filters.query} onChange={(e) => setFilters({ ...filters, query: e.target.value })} />
              <label className="field-label mt-3">Status</label>
              <select className="field-input" value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
                <option value="">Todos</option>{Object.entries(statusLabels).map(([value, label]) => <option value={value} key={value}>{label}</option>)}
              </select>
              <label className="field-label mt-3">Tipo</label>
              <select className="field-input" value={filters.type} onChange={(e) => setFilters({ ...filters, type: e.target.value })}>
                <option value="">Todos</option>{Object.entries(typeLabels).map(([value, label]) => <option value={value} key={value}>{label}</option>)}
              </select>
              <label className="form-check mt-3"><input type="checkbox" className="form-check-input" checked={filters.unreadOnly} onChange={(e) => setFilters({ ...filters, unreadOnly: e.target.checked })} /> Apenas nao lidas</label>
            </aside>
            <div className="inbox-list" aria-busy={loading}>
              {entries.map((entry) => (
                <button className={`inbox-row ${entry.readAt ? "" : "unread"} ${selected?.id === entry.id ? "active" : ""}`} key={entry.id} onClick={() => openEntry(entry)}>
                  <span className={`inbox-priority priority-${entry.priority.toLowerCase()}`}></span>
                  <div><strong>{entry.title}</strong><small>{entry.senderName || "Sistema"} · {typeLabels[entry.type]}</small></div>
                  <span className={`inbox-status status-${entry.status.toLowerCase()}`}>{statusLabels[entry.status]}</span>
                </button>
              ))}
              {!loading && entries.length === 0 && <div className="empty-state">Nenhuma entrada encontrada.</div>}
            </div>
            <article className="inbox-detail">
              {selected ? <>
                <p className="eyebrow dark mb-1">{typeLabels[selected.type]}</p>
                <h3>{selected.title}</h3>
                <p>{selected.description || "Sem descricao."}</p>
                <dl><dt>Destino</dt><dd>{selected.destinationEmployeeName || selected.destinationSectorName || "Prefeitura"}</dd><dt>Status</dt><dd>{statusLabels[selected.status]}</dd>{selected.assignedToName && <><dt>Responsavel</dt><dd>{selected.assignedToName}</dd></>}</dl>
                <div className="d-flex flex-wrap gap-2">
                  {selected.objectType === "cross_sector_task_request" && selected.status !== "COMPLETED" && <><button className="btn btn-primary" onClick={() => answerRequest(true)}>Aceitar e criar tarefa</button><button className="btn btn-outline-danger" onClick={() => answerRequest(false)}>Recusar</button></>}
                  {selected.status === "NEW" && !selected.destinationEmployeeId && <button className="btn btn-outline-primary" onClick={() => action("claim")}>Assumir</button>}
                  {!["COMPLETED", "ARCHIVED"].includes(selected.status) && <button className="btn btn-primary" onClick={() => action("complete")}>Concluir</button>}
                  {selected.url && <Link className="btn btn-outline-secondary" to={selected.url}>Abrir origem</Link>}
                </div>
              </> : <div className="empty-state">Selecione uma entrada para ver os detalhes.</div>}
            </article>
          </section>
        </div>
      </main>
    </DashboardLayout>
  );
}
