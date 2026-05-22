import { useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import {
  EmptyState,
  FieldLabel,
  IconButton,
  Modal,
  PageHeader,
  ProfileBadge,
  StatusBadge,
} from "../components/DashboardShared.jsx";
import {
  canManageCityTools,
  initials,
  slugify,
  useCityHallName,
  useEmployees,
  useJobs,
  useSectors,
} from "../services/mockupService.js";

export default function EmployeesPage() {
  const cityHallName = useCityHallName();
  const canManage = canManageCityTools();
  const [sectors] = useSectors();
  const [employees, setEmployees] = useEmployees();
  const [jobs] = useJobs();
  const [search, setSearch] = useState("");
  const [sector, setSector] = useState("");
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({
    name: "",
    email: "",
    cpf: "",
    phone: "",
    registration: "",
    sector: "",
    job: "",
    profile: "Servidor",
    active: true,
  });

  const filteredEmployees = useMemo(() => {
    const query = search.trim().toLowerCase();
    return employees.filter((employee) => {
      const matchesSearch = !query || [employee.name, employee.email].join(" ").toLowerCase().includes(query);
      const matchesSector = !sector || employee.sector === sector;
      return matchesSearch && matchesSector;
    });
  }, [employees, search, sector]);

  function openCreate() {
    setForm({
      name: "",
      email: "",
      cpf: "",
      phone: "",
      registration: "",
      sector: "",
      job: "",
      profile: "Servidor",
      active: true,
    });
    setModal({ type: "create" });
  }

  function openEdit(employee) {
    setForm({
      ...employee,
      cpf: employee.cpf || "",
      phone: employee.phone || "",
      registration: employee.registration || "",
    });
    setModal({ type: "edit", id: employee.id });
  }

  function saveEmployee(event) {
    event.preventDefault();
    const nextEmployee = {
      ...form,
      id: form.id || slugify(form.email || form.name) || `${Date.now()}`,
      sector: form.sector || "-",
      job: form.job || "-",
    };

    setEmployees((current) => {
      const exists = current.some((employee) => employee.id === nextEmployee.id);
      return exists
        ? current.map((employee) => (employee.id === nextEmployee.id ? nextEmployee : employee))
        : [nextEmployee, ...current];
    });
    setModal(null);
  }

  function toggleEmployee(id) {
    setEmployees((current) =>
      current.map((employee) => (employee.id === id ? { ...employee, active: !employee.active } : employee)),
    );
  }

  return (
    <DashboardLayout styles={["/css/management.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader
            eyebrow={cityHallName}
            title={"Funcion\u00e1rios"}
            action={
              canManage ? (
                <button className="btn btn-primary" type="button" onClick={openCreate}>
                  <i className="bi bi-person-plus-fill"></i> Novo funcion&aacute;rio
                </button>
              ) : null
            }
          />

          <section className="panel">
            <div className="row g-2 align-items-end mb-3">
              <div className="col-md-5">
                <FieldLabel>Buscar</FieldLabel>
                <input
                  className="form-control"
                  placeholder="Nome ou email"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />
              </div>
              <div className="col-md-4">
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
                <button className="btn btn-primary w-100" type="button">
                  <i className="bi bi-search"></i> Filtrar
                </button>
              </div>
            </div>

            <div className="table-responsive">
              <table className="table align-middle management-table">
                <thead>
                  <tr>
                    <th>Funcion&aacute;rio</th>
                    <th>Setor</th>
                    <th>Cargo</th>
                    <th>Perfil</th>
                    <th>Status</th>
                    {canManage && <th className="text-end">A&ccedil;&otilde;es</th>}
                  </tr>
                </thead>
                <tbody>
                  {filteredEmployees.map((employee) => (
                    <tr key={employee.id}>
                      <td>
                        <div className="user-cell">
                          <span className="avatar small">{initials(employee.name)}</span>
                          <div>
                            <h4 className="mb-0">{employee.name}</h4>
                            <small>{employee.email}</small>
                          </div>
                        </div>
                      </td>
                      <td>{employee.sector || "-"}</td>
                      <td>{employee.job || "-"}</td>
                      <td>
                        <ProfileBadge variant={employee.profile === "Admin cidade" ? "primary" : "light"}>
                          {employee.profile}
                        </ProfileBadge>
                      </td>
                      <td>
                        <StatusBadge active={employee.active} />
                      </td>
                      {canManage && (
                        <td className="text-end">
                          <IconButton icon="bi-pencil" title="Editar" onClick={() => openEdit(employee)} />
                          <IconButton
                            icon="bi-slash-circle"
                            title="Ativar/desativar"
                            danger
                            onClick={() => toggleEmployee(employee.id)}
                          />
                        </td>
                      )}
                    </tr>
                  ))}

                  {filteredEmployees.length === 0 && (
                    <tr>
                      <td colSpan={canManage ? 6 : 5}>
                        <EmptyState icon="bi-people">Nenhum funcion&aacute;rio encontrado.</EmptyState>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </section>
        </div>

        <Modal
          open={Boolean(modal)}
          title={modal?.type === "edit" ? `Editar ${form.name}` : "Novo funcion\u00e1rio"}
          onClose={() => setModal(null)}
          size="lg"
          footer={
            <button className="btn btn-primary" type="submit" form="employeeForm">
              {modal?.type === "edit" ? "Salvar alteracoes" : "Cadastrar e gerar senha"}
            </button>
          }
        >
          <form id="employeeForm" onSubmit={saveEmployee}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Nome</label>
                <input
                  className="form-control"
                  required
                  value={form.name}
                  onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Email</label>
                <input
                  className="form-control"
                  type="email"
                  required
                  value={form.email}
                  onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">CPF</label>
                <input
                  className="form-control"
                  value={form.cpf}
                  onChange={(event) => setForm((current) => ({ ...current, cpf: event.target.value }))}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Celular</label>
                <input
                  className="form-control"
                  value={form.phone}
                  onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Registro</label>
                <input
                  className="form-control"
                  value={form.registration}
                  onChange={(event) => setForm((current) => ({ ...current, registration: event.target.value }))}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Setor</label>
                <select
                  className="form-select"
                  value={form.sector}
                  onChange={(event) => setForm((current) => ({ ...current, sector: event.target.value }))}
                >
                  <option value="">-</option>
                  {sectors.map((item) => (
                    <option value={item.name} key={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label">Cargo</label>
                <select
                  className="form-select"
                  value={form.job}
                  onChange={(event) => setForm((current) => ({ ...current, job: event.target.value }))}
                >
                  <option value="">-</option>
                  {jobs.map((item) => (
                    <option value={item.name} key={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label">Perfil</label>
                <select
                  className="form-select"
                  value={form.profile}
                  onChange={(event) => setForm((current) => ({ ...current, profile: event.target.value }))}
                >
                  <option>Servidor</option>
                  <option>Admin cidade</option>
                </select>
              </div>
              <div className="col-md-4 d-flex align-items-end">
                <label className="form-check">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    checked={form.active}
                    onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))}
                  />
                  <span className="form-check-label">Ativo</span>
                </label>
              </div>
            </div>
          </form>
        </Modal>
      </main>
    </DashboardLayout>
  );
}
