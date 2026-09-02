import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import {
  AccessDenied,
  EmptyState,
  FieldLabel,
  IconButton,
  Modal,
  PageHeader,
  StatusBadge,
} from "../components/DashboardShared.jsx";
import { api, getSelectedCityHall, getStoredUser, getUserType } from "../services/api.js";

const PAGE_SIZE = 12;
const emptyForm = { id: null, name: "", slug: "", description: "", active: true };

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

function cityHallName() {
  const selected = getSelectedCityHall();
  if (selected?.name) return selected.name;
  const user = getStoredUser() || {};
  if (user.cityHall?.name) return user.cityHall.name;
  if (typeof user.cityHall === "string" && user.cityHall.trim()) return user.cityHall;
  if (user.prefeitura?.name) return user.prefeitura.name;
  if (typeof user.prefeitura === "string" && user.prefeitura.trim()) return user.prefeitura;
  return "Prefeitura vinculada";
}

function normalizeSector(item) {
  return {
    id: item.id,
    name: item.name || "Setor sem nome",
    slug: item.slug || slugify(item.name),
    description: item.description || "",
    active: item.active ?? true,
  };
}

export default function SectorsPage() {
  const canManage = ["admin_cidade", "admin_equipe"].includes(getUserType(getStoredUser()));
  const [sectors, setSectors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [modal, setModal] = useState(null);
  const [saving, setSaving] = useState(false);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [viewMode, setViewMode] = useState(() => {
    try {
      return localStorage.getItem("integra-resource-view-setores") === "table" ? "table" : "cards";
    } catch {
      return "cards";
    }
  });
  const [form, setForm] = useState(emptyForm);

  useEffect(() => {
    if (!canManage) {
      setLoading(false);
      return undefined;
    }
    let mounted = true;
    api.getSectors()
      .then((payload) => { if (mounted) setSectors(pageItems(payload).map(normalizeSector)); })
      .catch((error) => { if (mounted) setMessage({ type: "error", text: error.message || "Nao foi possivel carregar os setores." }); })
      .finally(() => { if (mounted) setLoading(false); });
    return () => { mounted = false; };
  }, [canManage]);

  useEffect(() => {
    try { localStorage.setItem("integra-resource-view-setores", viewMode); } catch { /* storage unavailable */ }
  }, [viewMode]);

  const filteredSectors = useMemo(() => {
    const query = search.trim().toLowerCase();
    return sectors
      .filter((sector) => {
        const matchesSearch = !query || `${sector.name} ${sector.slug} ${sector.description}`.toLowerCase().includes(query);
        const matchesStatus = !status || (status === "ativos" ? sector.active : !sector.active);
        return matchesSearch && matchesStatus;
      })
      .sort((a, b) => a.name.localeCompare(b.name, "pt-BR"));
  }, [sectors, search, status]);

  const pageCount = Math.max(1, Math.ceil(filteredSectors.length / PAGE_SIZE));
  const currentPage = Math.min(page, pageCount - 1);
  const visibleSectors = filteredSectors.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);

  function changeFilter(setter, value) {
    setter(value);
    setPage(0);
  }

  function openCreate() {
    setForm({ ...emptyForm });
    setModal("create");
  }

  function openEdit(sector) {
    setForm({ ...sector });
    setModal("edit");
  }

  function closeModal() {
    setModal(null);
    setForm(emptyForm);
  }

  async function saveSector(event) {
    event.preventDefault();
    setSaving(true);
    const payload = {
      name: form.name.trim(),
      slug: form.slug.trim(),
      description: form.description.trim(),
      active: form.active,
    };
    try {
      const saved = modal === "edit" ? await api.updateSector(form.id, payload) : await api.createSector(payload);
      const normalized = normalizeSector(saved);
      setSectors((current) => modal === "edit" ? current.map((item) => item.id === normalized.id ? normalized : item) : [normalized, ...current]);
      setMessage({ type: "success", text: modal === "edit" ? "Setor atualizado." : "Setor cadastrado." });
      closeModal();
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel salvar o setor." });
    } finally {
      setSaving(false);
    }
  }

  async function toggleSector(sector) {
    try {
      const updated = normalizeSector(await api.toggleSector(sector.id));
      setSectors((current) => current.map((item) => item.id === updated.id ? updated : item));
      setMessage({ type: "success", text: "Status do setor atualizado." });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel atualizar o status." });
    }
  }

  if (!canManage) {
    return <DashboardLayout styles={["/css/setores.css", "/css/management.css"]}><main className="dashboard"><div className="container"><AccessDenied /></div></main></DashboardLayout>;
  }

  return (
    <DashboardLayout styles={["/css/setores.css", "/css/management.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader eyebrow={cityHallName()} title="Setores" action={<button className="btn btn-primary" type="button" onClick={openCreate}><i className="bi bi-building-add"></i> Novo setor</button>} />
          {message && <div className={`auth-message ${message.type} mb-3`}><i className="bi bi-info-circle-fill"></i> {message.text}</div>}

          <section className="panel">
            <form className="row g-2 align-items-end mb-3" onSubmit={(event) => event.preventDefault()}>
              <div className="col-md-6"><FieldLabel>Buscar</FieldLabel><input className="form-control" placeholder="Buscar setor por nome" value={search} onChange={(event) => changeFilter(setSearch, event.target.value)} /></div>
              <div className="col-md-3"><FieldLabel>Status</FieldLabel><select className="form-select" value={status} onChange={(event) => changeFilter(setStatus, event.target.value)}><option value="">Todos</option><option value="ativos">Ativos</option><option value="inativos">Inativos</option></select></div>
              <div className="col-md-3"><button className="btn btn-primary w-100" type="submit"><i className="bi bi-search"></i> Filtrar</button></div>
            </form>

            <div className="resource-view-toolbar">
              <span>{visibleSectors.length} {visibleSectors.length === 1 ? "setor" : "setores"} nesta página</span>
              <div className="resource-view-toggle" role="group" aria-label="Formato de visualização">
                <button type="button" className={`resource-view-button ${viewMode === "cards" ? "is-active" : ""}`} aria-pressed={viewMode === "cards"} onClick={() => setViewMode("cards")}><i className="bi bi-grid-fill"></i> Cards</button>
                <button type="button" className={`resource-view-button ${viewMode === "table" ? "is-active" : ""}`} aria-pressed={viewMode === "table"} onClick={() => setViewMode("table")}><i className="bi bi-table"></i> Tabela</button>
              </div>
            </div>

            <div className="setores-grid" data-view-panel="cards" hidden={viewMode !== "cards"}>
              {loading ? <EmptyState icon="bi-arrow-repeat">Carregando setores...</EmptyState> : visibleSectors.length === 0 ? <EmptyState icon="bi-building">Nenhum setor encontrado.</EmptyState> : visibleSectors.map((sector) => (
                <article className="setor-card" key={sector.id}>
                  <div className="setor-card-main"><div className="setor-icon"><i className="bi bi-diagram-3-fill"></i></div><div><h4 className="mb-0 setor-title">{sector.name}</h4><div className="setor-meta">{sector.slug}</div></div></div>
                  <p>{sector.description || "Sem descricao cadastrada."}</p>
                  <div className="setor-footer"><StatusBadge active={sector.active} /><div className="d-flex gap-1"><IconButton icon="bi-pencil" title="Editar" onClick={() => openEdit(sector)} /><IconButton icon="bi-slash-circle" title="Ativar/desativar" danger onClick={() => toggleSector(sector)} /></div></div>
                </article>
              ))}
            </div>

            <div className="resource-table-wrap" data-view-panel="table" hidden={viewMode !== "table"}>
              <table className="resource-table"><thead><tr><th>Setor</th><th>Identificador</th><th>Descrição</th><th>Status</th><th className="resource-actions-column">Ações</th></tr></thead><tbody>
                {loading ? <tr><td className="resource-table-empty" colSpan="5">Carregando setores...</td></tr> : visibleSectors.length === 0 ? <tr><td className="resource-table-empty" colSpan="5">Nenhum setor encontrado.</td></tr> : visibleSectors.map((sector) => <tr key={sector.id}><td><span className="resource-table-name"><i className="bi bi-diagram-3-fill"></i> {sector.name}</span></td><td><code>{sector.slug}</code></td><td className="resource-description-cell">{sector.description ? `${sector.description.slice(0, 120)}${sector.description.length > 120 ? "..." : ""}` : "Sem descricao cadastrada."}</td><td><StatusBadge active={sector.active} /></td><td><div className="resource-table-actions"><IconButton icon="bi-pencil" title={`Editar ${sector.name}`} onClick={() => openEdit(sector)} /><IconButton icon="bi-slash-circle" title={`Ativar ou desativar ${sector.name}`} danger onClick={() => toggleSector(sector)} /></div></td></tr>)}
              </tbody></table>
            </div>

            {pageCount > 1 && <nav className="pagination-shell" aria-label="Paginação"><div className="pagination-actions"><button className="pagination-btn" type="button" disabled={currentPage === 0} onClick={() => setPage(currentPage - 1)} aria-label="Página anterior"><i className="bi bi-chevron-left"></i></button><span className="pagination-current">{currentPage + 1}/{pageCount}</span><button className="pagination-btn" type="button" disabled={currentPage === pageCount - 1} onClick={() => setPage(currentPage + 1)} aria-label="Próxima página"><i className="bi bi-chevron-right"></i></button></div></nav>}
          </section>
        </div>

        <Modal open={Boolean(modal)} title={modal === "edit" ? `Editar ${form.name}` : "Novo setor"} onClose={closeModal} footer={<button className="btn btn-primary" type="submit" form="sectorForm" disabled={saving}>{saving ? "Salvando..." : modal === "edit" ? "Salvar alterações" : "Cadastrar setor"}</button>}>
          <form id="sectorForm" onSubmit={saveSector}>
            <div className="mb-3"><label className="form-label">Nome</label><input className="form-control" required maxLength="120" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} /></div>
            <div className="mb-3"><label className="form-label">Identificador</label><input className="form-control" maxLength="140" value={form.slug} onChange={(event) => setForm((current) => ({ ...current, slug: event.target.value }))} placeholder={slugify(form.name)} /><small className="text-muted">Deixe em branco para gerar automaticamente.</small></div>
            <div className="mb-3"><label className="form-label">Descrição</label><textarea className="form-control" rows="3" maxLength="1000" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}></textarea></div>
            <label className="form-check"><input className="form-check-input" type="checkbox" checked={form.active} onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))} /><span className="form-check-label">Ativo</span></label>
          </form>
        </Modal>
      </main>
    </DashboardLayout>
  );
}
