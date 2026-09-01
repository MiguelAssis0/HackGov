import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { api, getSelectedCityHall, getStoredUser, getUserType } from "../services/api.js";

const emptyFilters = { q: "", prefeitura: "", tipo: "todos", modulo: "", acao: "", usuario: "", dataInicial: "", dataFinal: "" };

function pageLabel(count) {
  return `${count} registro${count === 1 ? "" : "s"} encontrado${count === 1 ? "" : "s"}.`;
}

function formatDate(value) {
  if (!value || value === "-") return "-";
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? value : parsed.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

function actionClass(action) {
  if (action === "DELETE") return "text-bg-danger";
  if (action === "UPDATE") return "text-bg-warning";
  if (["CREATE", "SEND"].includes(action)) return "text-bg-success";
  return "text-bg-primary";
}

function Pagination({ page, totalPages, first, last, onChange }) {
  if (!totalPages || totalPages <= 1) return null;
  return <nav className="audit-pagination" aria-label="Paginação dos registros de auditoria">
    <button type="button" className="btn btn-outline-secondary" disabled={first} onClick={() => onChange(page - 1)}><i className="bi bi-chevron-left"></i> Anterior</button>
    <span>Página {page + 1} de {totalPages}</span>
    <button type="button" className="btn btn-outline-secondary" disabled={last} onClick={() => onChange(page + 1)}>Próxima <i className="bi bi-chevron-right"></i></button>
  </nav>;
}

export default function AuditPage() {
  const platformAdmin = getUserType(getStoredUser() || {}) === "admin_equipe";
  const [scope, setScope] = useState(platformAdmin ? "global" : "prefeitura");
  const [filters, setFilters] = useState(() => ({ ...emptyFilters, prefeitura: platformAdmin ? "" : getSelectedCityHall()?.id || "" }));
  const [query, setQuery] = useState(() => ({ ...emptyFilters, scope: platformAdmin ? "global" : "prefeitura", prefeitura: platformAdmin ? "" : getSelectedCityHall()?.id || "", page: 0 }));
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    setLoading(true);
    api.getAuditEvents(query).then((response) => {
      if (!mounted) return;
      setData(response);
      setError("");
    }).catch((reason) => {
      if (mounted) setError(reason.message || "Não foi possível carregar os registros de auditoria.");
    }).finally(() => mounted && setLoading(false));
    return () => { mounted = false; };
  }, [query]);

  const setField = (field, value) => setFilters((current) => ({ ...current, [field]: value }));
  const applyFilters = (event) => {
    event.preventDefault();
    setQuery({ ...filters, scope, page: 0 });
  };
  const changeScope = (nextScope) => {
    setScope(nextScope);
    setFilters((current) => ({ ...current, prefeitura: nextScope === "global" ? "" : current.prefeitura }));
    setQuery((current) => ({ ...current, scope: nextScope, page: 0, prefeitura: nextScope === "global" ? "" : current.prefeitura }));
  };
  const clearFilters = () => {
    const next = { ...emptyFilters, prefeitura: scope === "prefeitura" && !platformAdmin ? getSelectedCityHall()?.id || "" : "" };
    setFilters(next);
    setQuery({ ...next, scope, page: 0 });
  };
  const rows = data?.content || [];
  const actionOptions = data?.actionOptions || [];
  const cityHallOptions = data?.cityHallOptions || [];
  const title = data?.scope === "global" ? "Logs Globais" : "Logs da Prefeitura";
  const subtitle = data?.scope === "global" ? "Todas as prefeituras" : "Trilha de eventos da prefeitura";
  const canExport = data?.canExport !== false;
  const sensitive = data?.canViewSensitive === true;
  const selectedCityName = useMemo(() => cityHallOptions.find((item) => item.id === filters.prefeitura)?.name, [cityHallOptions, filters.prefeitura]);

  return <DashboardLayout styles={["/css/auditoria.css"]}><main className="dashboard"><div className="container">
    <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2 audit-header"><div><p className="eyebrow dark mb-0">{subtitle}</p><h3 className="audit-title mb-0 fw-bold">{title}</h3></div><div className="audit-header-actions">{canExport && <button type="button" className="btn btn-primary" onClick={() => api.exportAudit(query)}><i className="bi bi-download"></i> Exportar CSV</button>}{platformAdmin && (scope === "global" ? <button type="button" className="btn btn-outline-primary" onClick={() => changeScope("prefeitura")}><i className="bi bi-building"></i> Prefeitura</button> : <button type="button" className="btn btn-outline-primary" onClick={() => changeScope("global")}><i className="bi bi-database-check"></i> Globais</button>)}</div></div>

    <section className="panel mb-3 audit-filter-panel"><form method="get" className="row g-2 align-items-end" onSubmit={applyFilters}>
      <div className="col-12 col-lg-3"><label className="audit-filter-label mb-1" htmlFor="audit-q">Buscar</label><input id="audit-q" className="form-control" value={filters.q} onChange={(event) => setField("q", event.target.value)} placeholder="Descrição, objeto ou usuário" /></div>
      {platformAdmin && <div className="col-12 col-md-4 col-lg-3"><label className="audit-filter-label mb-1" htmlFor="audit-city">Prefeitura</label><select id="audit-city" className="form-select" value={filters.prefeitura} onChange={(event) => setField("prefeitura", event.target.value)}><option value="">Todas</option>{cityHallOptions.map((city) => <option value={city.id} key={city.id}>{city.name}</option>)}</select></div>}
      <div className="col-12 col-md-4 col-lg-2"><label className="audit-filter-label mb-1" htmlFor="audit-type">Tipo</label><select id="audit-type" className="form-select" value={filters.tipo} onChange={(event) => setField("tipo", event.target.value)}><option value="todos">Todos</option><option value="manual">Evento manual</option><option value="automatico">Auditlog automático</option></select></div>
      <div className="col-12 col-md-4 col-lg-2"><label className="audit-filter-label mb-1" htmlFor="audit-module">Módulo</label><input id="audit-module" className="form-control" value={filters.modulo} onChange={(event) => setField("modulo", event.target.value)} placeholder="documentos" /></div>
      <div className="col-12 col-md-4 col-lg-2"><label className="audit-filter-label mb-1" htmlFor="audit-action">Ação</label><select id="audit-action" className="form-select" value={filters.acao} onChange={(event) => setField("acao", event.target.value)}><option value="">Todas</option>{actionOptions.map((action) => <option value={action.value} key={action.value}>{action.label}</option>)}</select></div>
      <div className="col-12 col-md-4 col-lg-3"><label className="audit-filter-label mb-1" htmlFor="audit-user">Usuário</label><input id="audit-user" className="form-control" value={filters.usuario} onChange={(event) => setField("usuario", event.target.value)} placeholder="Nome ou email" /></div>
      <div className="col-6 col-md-4 col-lg-2"><label className="audit-filter-label mb-1" htmlFor="audit-start">Data inicial</label><input id="audit-start" className="form-control" type="date" value={filters.dataInicial} onChange={(event) => setField("dataInicial", event.target.value)} /></div>
      <div className="col-6 col-md-4 col-lg-2"><label className="audit-filter-label mb-1" htmlFor="audit-end">Data final</label><input id="audit-end" className="form-control" type="date" value={filters.dataFinal} onChange={(event) => setField("dataFinal", event.target.value)} /></div>
      <div className="col-12 col-md-4 col-lg-2"><button className="btn btn-primary w-100" type="submit"><i className="bi bi-search"></i> Filtrar</button></div>
      <div className="col-12 col-md-4 col-lg-2"><button className="btn btn-outline-secondary w-100" type="button" onClick={clearFilters}>Limpar</button></div>
    </form>{selectedCityName && <small className="audit-selected-city"><i className="bi bi-building"></i> {selectedCityName}</small>}</section>

    {error && <div className="auth-message danger mb-3" role="alert">{error}</div>}
    <section className="panel audit-records-panel"><div className="panel-heading"><div><h3><i className="bi bi-shield-check me-2 text-primary"></i>Registros</h3><p>{pageLabel(data?.totalElements || 0)}</p></div></div><div className="table-responsive audit-table-wrap"><table className="table align-middle audit-table"><thead><tr><th>Data/Hora</th><th>Usuário</th><th>Prefeitura</th><th>Módulo</th><th>Ação</th><th>Resultado</th><th>Objeto</th><th>Descrição</th><th>IP</th></tr></thead><tbody>{rows.map((row) => <tr key={row.id}><td className="audit-table-nowrap">{formatDate(row.dataHora)}</td><td>{sensitive ? row.usuario : row.usuarioMascarado}</td><td>{row.prefeitura}</td><td>{row.modulo}</td><td><span className={`badge ${actionClass(row.acao)}`}>{row.acao}</span></td><td>{row.resultado}</td><td>{row.objeto}</td><td className="audit-table-description">{row.descricao}</td><td className="audit-table-nowrap"><span className="badge text-bg-light">{row.tipo}</span><span className="ms-1">{sensitive ? row.ip : "restrito"}</span></td></tr>)}{!loading && rows.length === 0 && <tr><td colSpan="9" className="empty-state">Nenhum log encontrado para os filtros selecionados.</td></tr>}</tbody></table>{loading && <div className="audit-loading"><i className="bi bi-arrow-repeat"></i> Carregando registros...</div>}</div><Pagination page={data?.page || 0} totalPages={data?.totalPages || 0} first={data?.first} last={data?.last} onChange={(page) => setQuery((current) => ({ ...current, page }))} /></section>
  </div></main></DashboardLayout>;
}
