import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { EmptyState, FieldLabel, Modal, PageHeader, StatusBadge } from "../components/DashboardShared.jsx";
import { api, getSelectedCityHall, getStoredUser } from "../services/api.js";

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
  const selectedCityHall = getSelectedCityHall();
  if (selectedCityHall?.name) return selectedCityHall.name;

  const user = getStoredUser() || {};
  const cityHall = user?.cityHall;
  if (cityHall?.name) return cityHall.name;
  if (typeof cityHall === "string" && cityHall.trim()) return cityHall;
  if (typeof user?.prefeitura === "string" && user.prefeitura.trim()) return user.prefeitura;
  if (user?.prefeitura?.name) return user.prefeitura.name;

  return "Prefeitura vinculada";
}

function normalizeOccupation(item) {
  return {
    id: item.id,
    name: item.name || "Cargo sem nome",
    slug: slugify(item.name),
    sector: item.sector || "",
    description: item.description || "",
    active: true,
    type: item.types || "",
    level: item.level || "",
  };
}

const emptyForm = {
  name: "",
  description: "",
  sectorId: "",
  types: "CONCURSADO",
  level: "JUNIOR",
};

export default function JobsPage() {
  const cityHallName = resolveCityHallName();
  const [sectors, setSectors] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [search, setSearch] = useState("");
  const [sector, setSector] = useState("");
  const [status, setStatus] = useState("");
  const [form, setForm] = useState(emptyForm);

  useEffect(() => {
    let mounted = true;

    async function loadJobsPage() {
      setLoading(true);
      setMessage(null);

      const [occupationsResult, sectorsResult] = await Promise.allSettled([api.getOccupations(), api.getSectors()]);

      if (!mounted) return;

      const nextJobs =
        occupationsResult.status === "fulfilled" ? pageItems(occupationsResult.value).map(normalizeOccupation) : [];
      const nextSectors = sectorsResult.status === "fulfilled" ? pageItems(sectorsResult.value) : [];
      const failures = [];

      if (occupationsResult.status === "rejected") failures.push("cargos");
      if (sectorsResult.status === "rejected") failures.push("setores");

      setJobs(nextJobs);
      setSectors(nextSectors);
      setLoading(false);

      if (failures.length > 0) {
        setMessage({
          type: "warning",
          text: `Nao foi possivel carregar ${failures.join(", ")} no backend. A pagina exibira apenas os dados disponiveis.`,
        });
      }
    }

    loadJobsPage();
    return () => {
      mounted = false;
    };
  }, []);

  const filteredJobs = useMemo(() => {
    const query = search.trim().toLowerCase();
    return jobs.filter((job) => {
      const matchesSearch = !query || [job.name, job.slug, job.sector, job.description].join(" ").toLowerCase().includes(query);
      const matchesSector = !sector || job.sector === sector;
      const matchesStatus = !status || (status === "ativos" && job.active) || (status === "inativos" && !job.active);
      return matchesSearch && matchesSector && matchesStatus;
    });
  }, [jobs, search, sector, status]);

  function openCreate() {
    setForm({
      ...emptyForm,
      sectorId: sectors[0]?.id || "",
    });
    setModalOpen(true);
  }

  function closeModal() {
    setModalOpen(false);
    setForm(emptyForm);
  }

  async function submitOccupation(event) {
    event.preventDefault();
    setSaving(true);

    try {
      const created = await api.createOccupation({
        name: form.name,
        description: form.description,
        sectorId: form.sectorId,
        types: form.types,
        level: form.level,
      });

      setJobs((current) => [normalizeOccupation(created), ...current]);
      setMessage({ type: "success", text: "Cargo criado com sucesso." });
      closeModal();
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel criar o cargo." });
    } finally {
      setSaving(false);
    }
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

          {message && (
            <div className={`auth-message ${message.type} mb-3`}>
              <i className="bi bi-info-circle-fill"></i>
              {message.text}
            </div>
          )}

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
              {loading ? (
                <EmptyState icon="bi-arrow-repeat">Carregando cargos...</EmptyState>
              ) : filteredJobs.length === 0 ? (
                <EmptyState icon="bi-person-badge-fill">Nenhum cargo encontrado.</EmptyState>
              ) : (
                filteredJobs.map((job) => (
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
                      <small className="setor-meta">{job.type || "Tipo nao informado"}</small>
                    </div>
                  </article>
                ))
              )}
            </div>
          </section>
        </div>

        <Modal
          open={modalOpen}
          title="Novo cargo"
          onClose={closeModal}
          footer={
            <button className="btn btn-primary" type="submit" form="occupationForm" disabled={saving}>
              {saving ? "Salvando..." : "Cadastrar cargo"}
            </button>
          }
        >
          <form id="occupationForm" onSubmit={submitOccupation}>
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
              <label className="form-label">Descricao</label>
              <textarea
                className="form-control"
                required
                rows="3"
                value={form.description}
                onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
              ></textarea>
            </div>
            <div className="mb-3">
              <label className="form-label">Setor</label>
              <select
                className="form-select"
                required
                value={form.sectorId}
                onChange={(event) => setForm((current) => ({ ...current, sectorId: event.target.value }))}
              >
                <option value="">Selecionar setor...</option>
                {sectors.map((item) => (
                  <option value={item.id} key={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Tipo</label>
                <select
                  className="form-select"
                  value={form.types}
                  onChange={(event) => setForm((current) => ({ ...current, types: event.target.value }))}
                >
                  <option value="CONCURSADO">Concursado</option>
                  <option value="CARGO_COMISSAO">Cargo comissao</option>
                  <option value="TERCEIRIZADO">Terceirizado</option>
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Nivel</label>
                <select
                  className="form-select"
                  value={form.level}
                  onChange={(event) => setForm((current) => ({ ...current, level: event.target.value }))}
                >
                  <option value="TRAINEE">Trainee</option>
                  <option value="JUNIOR">Junior</option>
                  <option value="MID">Mid</option>
                  <option value="SENIOR">Senior</option>
                  <option value="LEAD">Lead</option>
                </select>
              </div>
            </div>
          </form>
        </Modal>
      </main>
    </DashboardLayout>
  );
}
