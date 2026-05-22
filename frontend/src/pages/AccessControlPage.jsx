import { useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link } from "../components/RouterContext.jsx";
import { IconButton, PageHeader } from "../components/DashboardShared.jsx";
import {
  slugify,
  useAvailableTools,
  useCityHallName,
  useJobs,
  usePermissions,
  useSectors,
} from "../services/mockupService.js";

export default function AccessControlPage() {
  const cityHallName = useCityHallName();
  const [sectors] = useSectors();
  const [jobs] = useJobs();
  const [permissions, setPermissions] = usePermissions();
  const availableTools = useAvailableTools();
  const [form, setForm] = useState({
    tool: "",
    sector: "",
    job: "",
    level: "Visualizar",
  });

  function persist(nextPermissions) {
    setPermissions(nextPermissions);
  }

  function addPermission(event) {
    event.preventDefault();
    if (!form.tool) return;

    const nextPermission = {
      ...form,
      id: `${slugify(form.tool)}-${slugify(form.sector || "todos")}-${slugify(form.job || "todos")}-${Date.now()}`,
      sector: form.sector || "Todos",
      job: form.job || "Todos",
    };

    persist([nextPermission, ...permissions]);
    setForm({ tool: "", sector: "", job: "", level: "Visualizar" });
  }

  function removePermission(id) {
    persist(permissions.filter((permission) => permission.id !== id));
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
                      <option value={tool.name} key={tool.id}>
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
                      <option value={sector.name} key={sector.id}>
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
                      <option value={job.name} key={job.id}>
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
                    <option>Visualizar</option>
                    <option>Editar</option>
                    <option>Gerenciar</option>
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
                      <td>{permission.tool}</td>
                      <td>{permission.sector || "Todos"}</td>
                      <td>{permission.job || "Todos"}</td>
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
