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
import { slugify, useCityHallName, useJobs, useSectors } from "../services/mockupService.js";

export default function JobsPage() {
  const cityHallName = useCityHallName();
  const [sectors] = useSectors();
  const [jobs, setJobs] = useJobs();
  const [search, setSearch] = useState("");
  const [sector, setSector] = useState("");
  const [status, setStatus] = useState("");
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({ name: "", slug: "", sector: "", active: true });

  const filteredJobs = useMemo(() => {
    const query = search.trim().toLowerCase();
    return jobs.filter((job) => {
      const matchesSearch = !query || [job.name, job.slug, job.sector].join(" ").toLowerCase().includes(query);
      const matchesSector = !sector || job.sector === sector;
      const matchesStatus =
        !status || (status === "ativos" && job.active) || (status === "inativos" && !job.active);
      return matchesSearch && matchesSector && matchesStatus;
    });
  }, [jobs, search, sector, status]);

  function persist(nextJobs) {
    setJobs(nextJobs);
  }

  function openCreate() {
    setForm({ name: "", slug: "", sector: "", active: true });
    setModal({ type: "create" });
  }

  function openEdit(job) {
    setForm(job);
    setModal({ type: "edit", id: job.id });
  }

  function saveJob(event) {
    event.preventDefault();
    const nextJob = {
      ...form,
      id: form.id || slugify(form.slug || form.name) || `${Date.now()}`,
      slug: form.slug || slugify(form.name),
    };

    const exists = jobs.some((job) => job.id === nextJob.id);
    persist(exists ? jobs.map((job) => (job.id === nextJob.id ? nextJob : job)) : [nextJob, ...jobs]);
    setModal(null);
  }

  function toggleJob(id) {
    persist(jobs.map((job) => (job.id === id ? { ...job, active: !job.active } : job)));
  }

  return (
    <DashboardLayout styles={["/css/setores.css", "/css/management.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader
            eyebrow={cityHallName}
            title="Cargos"
            action={
              <button className="btn btn-primary" type="button" onClick={openCreate}>
                <i className="bi bi-person-badge-fill"></i> Novo cargo
              </button>
            }
          />

          <section className="panel">
            <div className="row g-2 align-items-end mb-3">
              <div className="col-md-4">
                <FieldLabel>Buscar</FieldLabel>
                <input
                  className="form-control"
                  placeholder="Buscar cargo por nome"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />
              </div>
              <div className="col-md-3">
                <FieldLabel>Setor</FieldLabel>
                <select className="form-select" value={sector} onChange={(event) => setSector(event.target.value)}>
                  <option value="">Todos</option>
                  {sectors.map((item) => (
                    <option value={item.name} key={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-3">
                <FieldLabel>Status</FieldLabel>
                <select className="form-select" value={status} onChange={(event) => setStatus(event.target.value)}>
                  <option value="">Todos</option>
                  <option value="ativos">Ativos</option>
                  <option value="inativos">Inativos</option>
                </select>
              </div>
              <div className="col-md-2">
                <button className="btn btn-primary w-100" type="button">
                  <i className="bi bi-search"></i> Filtrar
                </button>
              </div>
            </div>

            <div className="setores-grid">
              {filteredJobs.map((job) => (
                <article className="setor-card" key={job.id}>
                  <div className="setor-card-main">
                    <div className="setor-icon">
                      <i className="bi bi-person-badge-fill"></i>
                    </div>
                    <div>
                      <h4 className="mb-0 setor-title">{job.name}</h4>
                      <div className="setor-meta">{job.slug}</div>
                    </div>
                  </div>
                  <p>
                    {job.sector
                      ? `Vinculado ao setor ${job.sector}.`
                      : "Cargo geral da prefeitura, sem setor especifico."}
                  </p>
                  <div className="setor-footer">
                    <StatusBadge active={job.active} />
                    <div className="d-flex gap-1">
                      <IconButton icon="bi-pencil" title="Editar" onClick={() => openEdit(job)} />
                      <IconButton icon="bi-slash-circle" title="Ativar/desativar" danger onClick={() => toggleJob(job.id)} />
                    </div>
                  </div>
                </article>
              ))}

              {filteredJobs.length === 0 && <EmptyState icon="bi-person-badge-fill">Nenhum cargo encontrado.</EmptyState>}
            </div>
          </section>
        </div>

        <Modal
          open={Boolean(modal)}
          title={modal?.type === "edit" ? `Editar ${form.name}` : "Novo cargo"}
          onClose={() => setModal(null)}
          footer={
            <button className="btn btn-primary" type="submit" form="jobForm">
              {modal?.type === "edit" ? "Salvar alteracoes" : "Cadastrar cargo"}
            </button>
          }
        >
          <form id="jobForm" onSubmit={saveJob}>
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
              <label className="form-label">Setor</label>
              <select
                className="form-select"
                value={form.sector}
                onChange={(event) => setForm((current) => ({ ...current, sector: event.target.value }))}
              >
                <option value="">Sem setor especifico</option>
                {sectors.map((item) => (
                  <option value={item.name} key={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
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
