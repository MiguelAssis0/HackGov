import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { EmptyState, FieldLabel, IconButton, Modal, PageHeader, StatusBadge } from "../components/DashboardShared.jsx";
import { api, getSelectedCityHall, getStoredUser } from "../services/api.js";

const PAGE_SIZE = 12;

function pageItems(payload) {
  if (Array.isArray(payload)) return payload;
  return payload?.content || payload?.items || [];
}

function slugify(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function resolveCityHallName() {
  const selected = getSelectedCityHall();
  if (selected?.name) return selected.name;
  const user = getStoredUser() || {};
  const cityHall = user.cityHall;
  if (cityHall?.name) return cityHall.name;
  if (typeof cityHall === "string" && cityHall.trim()) return cityHall;
  if (typeof user.prefeitura === "string" && user.prefeitura.trim()) return user.prefeitura;
  if (user.prefeitura?.name) return user.prefeitura.name;
  return "Prefeitura vinculada";
}

function normalizeOccupation(item) {
  const sectorId = item.sectorId?.id || item.sectorId || item.sector?.id || "";
  return {
    id: item.id,
    name: item.name || "Cargo sem nome",
    slug: item.slug || slugify(item.name),
    sectorId,
    sector: item.sector?.name || item.sector || "",
    active: item.active ?? item.status ?? true,
  };
}

const emptyForm = { id: null, name: "", slug: "", sectorId: "", active: true };

export default function JobsPage() {
  const cityHallName = resolveCityHallName();
  const [sectors, setSectors] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [modal, setModal] = useState(null);
  const [saving, setSaving] = useState(false);
  const [search, setSearch] = useState("");
  const [sector, setSector] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [viewMode, setViewMode] = useState(() => {
    try {
      return localStorage.getItem("integra-resource-view-cargos") === "table" ? "table" : "cards";
    } catch {
      return "cards";
    }
  });
  const [form, setForm] = useState(emptyForm);

  useEffect(() => {
    let mounted = true;
    async function load() {
      setLoading(true);
      setMessage(null);
      const [jobsResult, sectorsResult] = await Promise.allSettled([api.getOccupations(), api.getSectors()]);
      if (!mounted) return;
      setJobs(jobsResult.status === "fulfilled" ? pageItems(jobsResult.value).map(normalizeOccupation) : []);
      setSectors(sectorsResult.status === "fulfilled" ? pageItems(sectorsResult.value).filter((item) => item.active ?? true) : []);
      setLoading(false);
      const failed = [jobsResult.status === "rejected" && "cargos", sectorsResult.status === "rejected" && "setores"].filter(Boolean);
      if (failed.length) setMessage({ type: "warning", text: `Nao foi possivel carregar ${failed.join(" e ")}.` });
    }
    load();
    return () => { mounted = false; };
  }, []);

  useEffect(() => {
    try { localStorage.setItem("integra-resource-view-cargos", viewMode); } catch { /* storage unavailable */ }
  }, [viewMode]);

  const filteredJobs = useMemo(() => {
    const query = search.trim().toLowerCase();
    return jobs
      .filter((job) => {
        const matchesSearch = !query || `${job.name} ${job.slug}`.toLowerCase().includes(query);
        const matchesSector = !sector || String(job.sectorId) === String(sector);
        const matchesStatus = !status || (status === "ativos" ? job.active : !job.active);
        return matchesSearch && matchesSector && matchesStatus;
      })
      .sort((a, b) => `${a.sector} ${a.name}`.localeCompare(`${b.sector} ${b.name}`, "pt-BR"));
  }, [jobs, search, sector, status]);

  const pageCount = Math.max(1, Math.ceil(filteredJobs.length / PAGE_SIZE));
  const currentPage = Math.min(page, pageCount - 1);
  const visibleJobs = filteredJobs.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);

  function changeFilter(setter, value) {
    setter(value);
    setPage(0);
  }

  function openCreate() {
    setForm({ ...emptyForm, sectorId: sectors[0]?.id || "" });
    setModal("create");
  }

  function openEdit(job) {
    setForm({ id: job.id, name: job.name, slug: job.slug, sectorId: job.sectorId || "", active: job.active });
    setModal("edit");
  }

  function closeModal() {
    setModal(null);
    setForm(emptyForm);
  }

  async function saveOccupation(event) {
    event.preventDefault();
    setSaving(true);
    const payload = { name: form.name.trim(), slug: form.slug.trim(), sectorId: form.sectorId || null, active: form.active };
    try {
      const saved = modal === "edit" ? await api.updateOccupation(form.id, payload) : await api.createOccupation(payload);
      const normalized = normalizeOccupation(saved);
      setJobs((current) => modal === "edit" ? current.map((job) => job.id === normalized.id ? normalized : job) : [normalized, ...current]);
      setMessage({ type: "success", text: modal === "edit" ? "Cargo atualizado." : "Cargo cadastrado." });
      closeModal();
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel salvar o cargo." });
    } finally {
      setSaving(false);
    }
  }

  async function toggleOccupation(job) {
    try {
      const updated = normalizeOccupation(await api.toggleOccupation(job.id));
      setJobs((current) => current.map((item) => item.id === updated.id ? updated : item));
      setMessage({ type: "success", text: "Status do cargo atualizado." });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel atualizar o status." });
    }
  }

  return (
    <DashboardLayout styles={["/css/setores.css", "/css/management.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader
            eyebrow={cityHallName}
            title="Cargos"
            action={<button className="btn btn-primary" type="button" onClick={openCreate}><i className="bi bi-person-badge-fill"></i> Novo cargo</button>}
          />

          {message && <div className={`auth-message ${message.type} mb-3`}><i className="bi bi-info-circle-fill"></i> {message.text}</div>}

          <section className="panel">
            <form className="row g-2 align-items-end mb-3" onSubmit={(event) => event.preventDefault()}>
              <div className="col-md-4"><FieldLabel>Buscar</FieldLabel><input className="form-control" placeholder="Buscar cargo por nome" value={search} onChange={(event) => changeFilter(setSearch, event.target.value)} /></div>
              <div className="col-md-3"><FieldLabel>Setor</FieldLabel><select className="form-select" value={sector} onChange={(event) => changeFilter(setSector, event.target.value)}><option value="">Todos</option>{sectors.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}</select></div>
              <div className="col-md-3"><FieldLabel>Status</FieldLabel><select className="form-select" value={status} onChange={(event) => changeFilter(setStatus, event.target.value)}><option value="">Todos</option><option value="ativos">Ativos</option><option value="inativos">Inativos</option></select></div>
              <div className="col-md-2"><button className="btn btn-primary w-100" type="submit"><i className="bi bi-search"></i> Filtrar</button></div>
            </form>

            <div className="resource-view-toolbar">
              <span>{visibleJobs.length} cargo{visibleJobs.length === 1 ? "" : "s"} nesta página</span>
              <div className="resource-view-toggle" role="group" aria-label="Formato de visualização">
                <button type="button" className={`resource-view-button ${viewMode === "cards" ? "is-active" : ""}`} aria-pressed={viewMode === "cards"} onClick={() => setViewMode("cards")}><i className="bi bi-grid-fill"></i> Cards</button>
                <button type="button" className={`resource-view-button ${viewMode === "table" ? "is-active" : ""}`} aria-pressed={viewMode === "table"} onClick={() => setViewMode("table")}><i className="bi bi-table"></i> Tabela</button>
              </div>
            </div>

            <div className="setores-grid" data-view-panel="cards" hidden={viewMode !== "cards"}>
              {loading ? <EmptyState icon="bi-arrow-repeat">Carregando cargos...</EmptyState> : visibleJobs.length === 0 ? <EmptyState icon="bi-person-badge-fill">Nenhum cargo encontrado.</EmptyState> : visibleJobs.map((job) => (
                <article className="setor-card" key={job.id}>
                  <div className="setor-card-main"><div className="setor-icon"><i className="bi bi-person-badge-fill"></i></div><div><h4 className="mb-0 setor-title">{job.name}</h4><div className="setor-meta">{job.slug}</div></div></div>
                  <p>{job.sector ? `Vinculado ao setor ${job.sector}.` : "Cargo geral da prefeitura, sem setor especifico."}</p>
                  <div className="setor-footer"><StatusBadge active={job.active} /><div className="d-flex gap-1"><IconButton icon="bi-pencil" title="Editar" onClick={() => openEdit(job)} /><IconButton icon="bi-slash-circle" title="Ativar/desativar" danger onClick={() => toggleOccupation(job)} /></div></div>
                </article>
              ))}
            </div>

            <div className="resource-table-wrap" data-view-panel="table" hidden={viewMode !== "table"}>
              <table className="resource-table"><thead><tr><th>Cargo</th><th>Identificador</th><th>Setor</th><th>Status</th><th className="resource-actions-column">Ações</th></tr></thead><tbody>
                {loading ? <tr><td className="resource-table-empty" colSpan="5">Carregando cargos...</td></tr> : visibleJobs.length === 0 ? <tr><td className="resource-table-empty" colSpan="5">Nenhum cargo encontrado.</td></tr> : visibleJobs.map((job) => <tr key={job.id}><td><span className="resource-table-name"><i className="bi bi-person-badge-fill"></i> {job.name}</span></td><td><code>{job.slug}</code></td><td>{job.sector || "Sem setor específico"}</td><td><StatusBadge active={job.active} /></td><td><div className="resource-table-actions"><IconButton icon="bi-pencil" title={`Editar ${job.name}`} onClick={() => openEdit(job)} /><IconButton icon="bi-slash-circle" title={`Ativar ou desativar ${job.name}`} danger onClick={() => toggleOccupation(job)} /></div></td></tr>)}
              </tbody></table>
            </div>

            {pageCount > 1 && <nav className="pagination-shell" aria-label="Paginação"><div className="pagination-actions"><button className={`pagination-btn ${currentPage === 0 ? "disabled" : ""}`} type="button" disabled={currentPage === 0} onClick={() => setPage(currentPage - 1)} aria-label="Página anterior"><i className="bi bi-chevron-left"></i></button><span className="pagination-current">{currentPage + 1}/{pageCount}</span><button className={`pagination-btn ${currentPage === pageCount - 1 ? "disabled" : ""}`} type="button" disabled={currentPage === pageCount - 1} onClick={() => setPage(currentPage + 1)} aria-label="Próxima página"><i className="bi bi-chevron-right"></i></button></div></nav>}
          </section>
        </div>

        <Modal open={Boolean(modal)} title={modal === "edit" ? `Editar ${form.name}` : "Novo cargo"} onClose={closeModal} footer={<button className="btn btn-primary" type="submit" form="occupationForm" disabled={saving}>{saving ? "Salvando..." : modal === "edit" ? "Salvar alterações" : "Cadastrar cargo"}</button>}>
          <form id="occupationForm" onSubmit={saveOccupation}>
            <div className="mb-3"><label className="form-label">Nome</label><input className="form-control" required maxLength="120" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} /></div>
            <div className="mb-3"><label className="form-label">Identificador</label><input className="form-control" maxLength="140" value={form.slug} onChange={(event) => setForm((current) => ({ ...current, slug: event.target.value }))} placeholder={slugify(form.name)} /><small className="text-muted">Deixe em branco para gerar automaticamente.</small></div>
            <div className="mb-3"><label className="form-label">Setor</label><select className="form-select" value={form.sectorId} onChange={(event) => setForm((current) => ({ ...current, sectorId: event.target.value }))}><option value="">Sem setor específico</option>{sectors.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}</select></div>
            <label className="form-check"><input className="form-check-input" type="checkbox" checked={form.active} onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))} /><span className="form-check-label">Ativo</span></label>
          </form>
        </Modal>
      </main>
    </DashboardLayout>
  );
}
