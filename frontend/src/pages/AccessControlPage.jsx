import { useEffect, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link } from "../components/RouterContext.jsx";
import { IconButton, PageHeader } from "../components/DashboardShared.jsx";
import { api } from "../services/api.js";
import {
  useAvailableTools,
  useCityHallName,
  useJobs,
  useSectors,
} from "../services/mockupService.js";

export default function AccessControlPage() {
  const cityHallName = useCityHallName();
  const [sectors] = useSectors();
  const [jobs] = useJobs();
  const [permissions, setPermissions] = useState([]);
  const [message, setMessage] = useState("");
  const availableTools = useAvailableTools();
  const [form, setForm] = useState({
    tool: "",
    sector: "",
    job: "",
    level: "VIEW",
  });

  useEffect(() => { api.getToolPermissions().then(setPermissions).catch((error) => setMessage(error.message)); }, []);

  async function addPermission(event) {
    event.preventDefault();
    if (!form.tool) return;

    try { const saved = await api.createToolPermission({ toolSlug: form.tool, sectorId: form.sector || null, occupationId: form.job || null, level: form.level, enabled: true }); setPermissions([saved, ...permissions]); setForm({ tool: "", sector: "", job: "", level: "VIEW" }); }
    catch (error) { setMessage(error.message); }
  }

  async function removePermission(id) {
    try { await api.deleteToolPermission(id); setPermissions(permissions.filter((permission) => permission.id !== id)); }
    catch (error) { setMessage(error.message); }
  }

  return (
    <DashboardLayout styles={["/css/ferramentas.css", "/css/management.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader
            eyebrow={cityHallName}
            title="Controle de Acesso"
            action={
              <Link to="/ferramentas" className="btn btn-primary">
                <i className="bi bi-grid-1x2-fill"></i> Ferramentas
              </Link>
            }
          />
          {message && <div className="auth-message danger mb-3">{message}</div>}

          <div className="ferr-permissoes-wrap">
            <div className="ferr-permissoes-header d-flex align-items-center justify-content-between">
              <div>
                <p className="eyebrow dark mb-0">Permiss&otilde;es</p>
                <h5 className="fw-bold mb-0">Acesso por setor e cargo</h5>
              </div>
              <span className="badge text-bg-light">
                {permissions.length} cadastrada{permissions.length === 1 ? "" : "s"}
              </span>
            </div>

            <div className="ferr-permissoes-body">
              <form className="row g-2 align-items-end" onSubmit={addPermission}>
                <div className="col-12 col-md-4">
                  <label className="management-field-label">Ferramenta</label>
                  <select
                    className="form-select"
                    value={form.tool}
                    onChange={(event) => setForm((current) => ({ ...current, tool: event.target.value }))}
                  >
                    <option value="">---------</option>
                    {availableTools.map((tool) => (
                      <option value={tool.id} key={tool.id}>
                        {tool.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-12 col-md-5">
                  <label className="management-field-label">Setor</label>
                  <select
                    className="form-select"
                    value={form.sector}
                    onChange={(event) => setForm((current) => ({ ...current, sector: event.target.value }))}
                  >
                    <option value="">---------</option>
                    {sectors.map((sector) => (
                      <option value={sector.id} key={sector.id}>
                        {sector.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-12 col-md-5">
                  <label className="management-field-label">Cargo</label>
                  <select
                    className="form-select"
                    value={form.job}
                    onChange={(event) => setForm((current) => ({ ...current, job: event.target.value }))}
                  >
                    <option value="">---------</option>
                    {jobs.map((job) => (
                      <option value={job.id} key={job.id}>
                        {job.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-12 col-md-2">
                  <label className="management-field-label">N&iacute;vel</label>
                  <select
                    className="form-select"
                    value={form.level}
                    onChange={(event) => setForm((current) => ({ ...current, level: event.target.value }))}
                  >
                    <option value="VIEW">Visualizar</option>
                    <option value="MANAGE">Editar</option>
                    <option value="ADMIN">Gerenciar</option>
                  </select>
                </div>
                <div className="col-12 col-md-auto">
                  <button type="submit" className="btn btn-primary w-100">
                    <span className="d-flex align-items-center justify-content-center">
                      <i className="bi bi-plus-lg me-1"></i>Adicionar
                    </span>
                  </button>
                </div>
              </form>
            </div>

            <div className="table-responsive">
              <table className="table align-middle mb-0 management-table">
                <thead>
                  <tr>
                    <th>Ferramenta</th>
                    <th>Setor</th>
                    <th>Cargo</th>
                    <th>N&iacute;vel</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {permissions.map((permission) => (
                    <tr key={permission.id}>
                      <td>{availableTools.find((tool) => tool.id === permission.toolSlug)?.name || permission.toolSlug}</td>
                      <td>{permission.sectorName || "Todos"}</td>
                      <td>{permission.occupationName || "Todos"}</td>
                      <td>
                        <span className="badge text-bg-light">{permission.level}</span>
                      </td>
                      <td className="text-end pe-3">
                        <IconButton
                          icon="bi-trash"
                          title="Remover"
                          danger
                          onClick={() => removePermission(permission.id)}
                        />
                      </td>
                    </tr>
                  ))}

                  {permissions.length === 0 && (
                    <tr>
                      <td colSpan="5" className="text-center text-muted py-4">
                        Nenhuma permiss&atilde;o espec&iacute;fica cadastrada.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </main>
    </DashboardLayout>
  );
}
