import { useEffect, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { PageHeader } from "../components/DashboardShared.jsx";
import { api } from "../services/api.js";

export default function AuditPage() {
  const [events, setEvents] = useState([]); const [query, setQuery] = useState(""); const [verification, setVerification] = useState(null); const [error, setError] = useState("");
  useEffect(() => { const timer = window.setTimeout(async () => { try { setEvents(await api.getAuditEvents(query)); setError(""); } catch (reason) { setError(reason.message); } }, 200); return () => window.clearTimeout(timer); }, [query]);
  async function verify() { try { const result = await api.verifyAuditChain(); setVerification(result.valid); } catch (reason) { setError(reason.message); } }
  return <DashboardLayout><main className="dashboard"><div className="container"><PageHeader eyebrow="Trilha imutavel por prefeitura" title="Auditoria" action={<button className="btn btn-primary" onClick={verify}><i className="bi bi-shield-check"></i> Verificar cadeia</button>} />
    {error && <div className="auth-message danger mb-3">{error}</div>}{verification !== null && <div className={`auth-message ${verification ? "success" : "danger"} mb-3`}>{verification ? "Cadeia de hashes integra." : "A cadeia apresenta inconsistencias."}</div>}
    <div className="document-toolbar"><i className="bi bi-search"></i><input className="field-input" placeholder="Buscar usuario, rota ou metodo" value={query} onChange={(event) => setQuery(event.target.value)} /></div>
    <div className="table-responsive"><table className="table align-middle"><thead><tr><th>Data</th><th>Usuario</th><th>Acao</th><th>Rota</th><th>Status</th><th>Hash</th></tr></thead><tbody>{events.map((event) => <tr key={event.id}><td>{new Date(event.createdAt).toLocaleString("pt-BR")}</td><td>{event.actorEmail}</td><td>{event.method}</td><td><code>{event.path}</code></td><td>{event.responseStatus}</td><td><code title={event.eventHash}>{event.eventHash.slice(0, 12)}...</code></td></tr>)}</tbody></table>{events.length === 0 && !error && <div className="empty-state">Nenhum evento de alteracao registrado.</div>}</div>
  </div></main></DashboardLayout>;
}
