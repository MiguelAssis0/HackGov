import { useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import {
  EmptyState,
  FieldLabel,
  IconButton,
  Modal,
  PageHeader,
  StatusBadge,
} from "../components/DashboardShared.jsx";
import { slugify, useCityHallName, useSectors } from "../services/mockupService.js";

export default function SectorsPage() {
  const cityHallName = useCityHallName();
  const [sectors, setSectors] = useSectors();
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({ name: "", slug: "", description: "", active: true });

  const filteredSectors = useMemo(() => {
    const query = search.trim().toLowerCase();
    return sectors.filter((sector) => {
      const matchesSearch = !query || [sector.name, sector.slug, sector.description].join(" ").toLowerCase().includes(query);
      const matchesStatus =
        !status || (status === "ativos" && sector.active) || (status === "inativos" && !sector.active);
      return matchesSearch && matchesStatus;
    });
  }, [sectors, search, status]);

  function openCreate() {
    setForm({ name: "", slug: "", description: "", active: true });
    setModal({ type: "create" });
  }

  function openEdit(sector) {
    setForm(sector);
    setModal({ type: "edit", id: sector.id });
  }

  function saveSector(event) {
    event.preventDefault();
    const nextSector = {
      ...form,
      id: form.id || slugify(form.slug || form.name) || `${Date.now()}`,
      slug: form.slug || slugify(form.name),
      description: form.description || "Sem descricao cadastrada.",
    };

    setSectors((current) => {
      const exists = current.some((sector) => sector.id === nextSector.id);
      if (exists) {
        return current.map((sector) => (sector.id === nextSector.id ? nextSector : sector));
      }
      return [nextSector, ...current];
    });
    setModal(null);
  }

  function toggleSector(id) {
    setSectors((current) =>
      current.map((sector) => (sector.id === id ? { ...sector, active: !sector.active } : sector)),
    );
  }

  return (
    <DashboardLayout styles={["/css/setores.css", "/css/management.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader
            eyebrow={cityHallName}
            title="Setores"
            action={
              <button className="btn btn-primary" type="button" onClick={openCreate}>
                <i className="bi bi-building-add"></i> Novo setor
              </button>
            }
          />

          <section className="panel">
            <div className="row g-2 align-items-end mb-3">
              <div className="col-md-6">
                <FieldLabel>Buscar</FieldLabel>
                <input
                  className="form-control"
                  placeholder="Buscar setor por nome"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />
              </div>
              <div className="col-md-3">
                <FieldLabel>Status</FieldLabel>
                <select className="form-select" value={status} onChange={(event) => setStatus(event.target.value)}>
                  <option value="">Todos</option>
                  <option value="ativos">Ativos</option>
                  <option value="inativos">Inativos</option>
                </select>
              </div>
              <div className="col-md-3">
                <button className="btn btn-primary w-100" type="button">
                  <i className="bi bi-search"></i> Filtrar
                </button>
              </div>
            </div>

            <div className="setores-grid">
              {filteredSectors.map((sector) => (
                <article className="setor-card" key={sector.id}>
                  <div className="setor-card-main">
                    <div className="setor-icon">
                      <i className="bi bi-diagram-3-fill"></i>
                    </div>
                    <div>
                      <h4 className="mb-0 setor-title">{sector.name}</h4>
                      <div className="setor-meta">{sector.slug}</div>
                    </div>
                  </div>
                  <p>{sector.description || "Sem descricao cadastrada."}</p>
                  <div className="setor-footer">
                    <StatusBadge active={sector.active} />
                    <div className="d-flex gap-1">
                      <IconButton icon="bi-pencil" title="Editar" onClick={() => openEdit(sector)} />
                      <IconButton icon="bi-slash-circle" title="Ativar/desativar" danger onClick={() => toggleSector(sector.id)} />
                    </div>
                  </div>
                </article>
              ))}

              {filteredSectors.length === 0 && <EmptyState icon="bi-building">Nenhum setor encontrado.</EmptyState>}
            </div>
          </section>
        </div>

        <Modal
          open={Boolean(modal)}
          title={modal?.type === "edit" ? `Editar ${form.name}` : "Novo setor"}
          onClose={() => setModal(null)}
          footer={
            <button className="btn btn-primary" type="submit" form="sectorForm">
              {modal?.type === "edit" ? "Salvar alteracoes" : "Cadastrar setor"}
            </button>
          }
        >
          <form id="sectorForm" onSubmit={saveSector}>
            <div className="mb-3">
              <label className="form-label">Nome</label>
              <input
                className="form-control"
                required
                value={form.name}
                onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
              />
            </div>
            <div className="mb-3">
              <label className="form-label">Identificador</label>
              <input
                className="form-control"
                value={form.slug}
                onChange={(event) => setForm((current) => ({ ...current, slug: event.target.value }))}
                placeholder={slugify(form.name)}
              />
              <small className="text-muted">Gerado automaticamente caso fique vazio.</small>
            </div>
            <div className="mb-3">
              <label className="form-label">Descricao</label>
              <textarea
                className="form-control"
                rows="3"
                value={form.description}
                onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
              ></textarea>
            </div>
            <label className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                checked={form.active}
                onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))}
              />
              <span className="form-check-label">Ativo</span>
            </label>
          </form>
        </Modal>
      </main>
    </DashboardLayout>
  );
}
